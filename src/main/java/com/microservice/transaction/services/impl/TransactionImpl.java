package com.microservice.transaction.services.impl;

import com.microservice.transaction.client.AccountServiceClient;
import com.microservice.transaction.config.AppConfig;
import com.microservice.transaction.entities.Transaction;
import com.microservice.transaction.entities.dtos.CreateTransactionDto;
import com.microservice.transaction.entities.dtos.CreateTransferDto;
import com.microservice.transaction.entities.dtos.ResponseAccountDto;
import com.microservice.transaction.entities.dtos.ResponseTransactionDto;
import com.microservice.transaction.entities.dtos.ResponseTransferDto;
import com.microservice.transaction.entities.dtos.TransactionDto;
import com.microservice.transaction.repositories.ITransactionRepository;
import com.microservice.transaction.services.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionImpl implements ITransactionService {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private AccountServiceClient accountServiceClient;

    public static final ModelMapper modelMapper=new ModelMapper();


    private void updateAmountHandler(Double amount, Double commission, String transactionType, String accountId){
        //Update amount
        TransactionDto transactionDto = TransactionDto.builder()
                .amount(amount) //Dependiendo de la cuenta y el numero de transacciones
                .commission(commission)
                .accountId(accountId)
                .transactionType(transactionType)
                .build();
        accountServiceClient.updateAmount(transactionDto,accountId);
    }

    public ResponseTransactionDto doTransaction (Double amount, Double commission, String transactionType, String accountId, 
    		String accountNumber, String customerId){
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .commission(commission)
                .transactionType(transactionType)
                .accountId(new ObjectId(accountId))
                .transactionDate(LocalDateTime.now())
                .build();
        transaction = transactionRepository.save(transaction);

        //Update amount
        updateAmountHandler(amount, commission, transactionType,accountId);

        //Map response
        ResponseTransactionDto response = ResponseTransactionDto.builder()
                ._id(transaction.get_id().toString())
                .accountNumber(accountNumber)
                .amount(amount)
                .commission(commission)
                .transactionType(transactionType)
                .customerId(customerId)
                .build();
        return response;
    }

    public ResponseTransactionDto doWithdraw(Double amount, Double commission, String transactionType, String accountId, 
    		String accountNumber, String customerId, Boolean customerFound) throws Exception{
        if(customerFound){
            return doTransaction(amount, commission, transactionType, accountId, accountNumber, customerId);
        }else{
            throw new Exception("CUSTOMER_NOT_FOUND_IN_ACCOUNT");
        }
    }

    @Override
    public ResponseTransactionDto createTransaction(CreateTransactionDto dto) throws Exception {
        //Validate transaction type
        String transactionType = appConfig.getTransactionTypeByName(dto.getTransactionType())
                .orElseThrow(()->new Exception("TRANSACTION_TYPE_NOT_FOUND"));

        System.out.println(dto.getCustomerId());
        //Validate account number
        ResponseAccountDto accountDto = accountServiceClient.findAccountByAccountNumber(dto.getAccountNumber())
                .orElseThrow(()->new Exception("ACCOUNT_NOT_FOUND"));

        //Validate account owner
        Boolean customerFound = accountDto.getCustomersIds().contains(dto.getCustomerId());
        
        //Original account - for business rules of debit card
        ResponseAccountDto original = accountDto;
        
        //If is a debit card and a transaction type "RETIRO"
        if(accountDto.getAccountType().getType().equals("TARJETA_DEBITO") && !accountDto.getAccountsIds().isEmpty()
        		&& (dto.getTransactionType().equals("RETIRO") || dto.getTransactionType().equals("PAGO"))) {
        	
        	//Obtain the information of the accounts associated with the debit card
        	List<String> accountNumbers = accountDto.getAccountsIds(); 
        	List<ResponseAccountDto> accounts = new ArrayList<>();
        	accountNumbers.forEach(num -> {
        		accounts.add(accountServiceClient.findAccountByAccountNumber(num).get());
        	});
        	
        	/* AccountDto must change to the main account (the first one) and if you do not have money or want to withdraw more 
        	 * money than the available balance, change to the next available account. */
        	accountDto = accounts.stream()
        			.filter(acc -> (acc.getBalance().compareTo(0.0) > 0 && acc.getBalance().compareTo(dto.getAmount()) >= 0))
        			.collect(Collectors.toList()).get(0);
        	
        	//If no account has the necessary balance, it gives an error
        	
        }
        
        //Validate number of transactions
        Double commission = 0.0;
        if(getTransactionsByAccountId(accountDto.get_id()).stream().count() >= accountDto.getAccountType().getMaxTransactionsPerMonth()) {
        	commission = accountDto.getAccountType().getCommissions();
        }
        
        //Create transaction
        ResponseTransactionDto response = new ResponseTransactionDto();
        switch (dto.getTransactionType()){
            case "DEPOSITO": 
                response =  doTransaction(dto.getAmount(), commission, transactionType, accountDto.get_id(), 
                		accountDto.getAccountNumber(),dto.getCustomerId());
               break;
            case "RETIRO": case "PAGO": //Also called "PAGO"
                response = doWithdraw(dto.getAmount(), commission, transactionType, accountDto.get_id(), 
                		accountDto.getAccountNumber(), dto.getCustomerId(), customerFound);
                
                if(original.getAccountType().getType().equals("TARJETA_DEBITO")) {
                	//Register transaction in debit card, but no response - Since the account balance is zero, it will return a negative value
                	doWithdraw(dto.getAmount(), commission, transactionType, original.get_id(), 
                    		original.getAccountNumber(), dto.getCustomerId(), customerFound);
                }
                break;

        }
        return response;
    }

    @Override
    public List<TransactionDto> getTransactionsByAccountId(String id) throws Exception {
        List<Transaction> transactions = transactionRepository.findByAccountId(new ObjectId(id));
        List<TransactionDto> transactionDtos = modelMapper.map(transactions,new TypeToken<List<TransactionDto>>(){}.getType());
        return transactionDtos;
    }

	@Override
	public ResponseTransactionDto createTransfer(CreateTransferDto dto) throws Exception {
		
		//Tipo de transacción
        String transactionType = appConfig.getTransactionTypeByName(dto.getTransactionType())
                .orElseThrow(()->new Exception("TRANSACTION_TYPE_NOT_FOUND"));
		
        //Validar cuenta origen
        ResponseAccountDto accountSourceDto = accountServiceClient.findAccountByAccountNumber(dto.getAccountNumberSource())
                .orElseThrow(()->new Exception("SOURCE_ACCOUNT_NOT_FOUND"));
        
        //Validar cuenta destino
        ResponseAccountDto accountDestinationDto = accountServiceClient.findAccountByAccountNumber(dto.getAccountNumberDestination())
                .orElseThrow(()->new Exception("DESTINATION_ACCOUNT_NOT_FOUND"));
        
        //Validar que la cuenta de origen sea del mismo cliente enviado en el body
        Boolean customerFound = accountSourceDto.getCustomersIds().contains(dto.getCustomerId());
        
        ResponseTransactionDto response = new ResponseTransactionDto();
        if(accountSourceDto.getBalance() > 0) {
        	switch (dto.getTransactionType()) {
            case "TRANSFERENCIA_CUENTA": //Solo entre cuentas de ahorro
                
                //A la cuenta de origen le descuentas lo que envia y eso devuelves
                response = doWithdraw(dto.getAmount(), 0.0, "TRANSFER_ENVIO", accountSourceDto.get_id(), accountSourceDto.getAccountNumber(), 
                		dto.getCustomerId(), customerFound);
                
                //A la cuenta de destino le aumentas, pero como es entre las mismas cuentas, la id de cliente es la misma
                doTransaction(dto.getAmount(), 0.0, "TRANSFER_RECIBIDO", accountDestinationDto.get_id(), accountDestinationDto.getAccountNumber(), 
                		dto.getCustomerId());
               break;
            case "CREDITO_TERCEROS": // Payment to third parties
            	
            	if((accountSourceDto.getAccountType().getProductType().equals("PASIVOS") || 
            			accountSourceDto.getAccountType().getType().equals("TARJETA_DEBITO")) && 
            			(accountDestinationDto.getAccountType().getProductType().equals("ACTIVOS") 
            					&& !accountDestinationDto.getCustomersIds().contains(dto.getCustomerId()))) {
            		
            		//If you have debt on your credit and what you will pay is less than or equal to what you owe
            		if(accountDestinationDto.getBalance() > 0 && dto.getAmount() <= accountDestinationDto.getBalance()) { 
            			//What you send is withdrawn from the origin account
                        response = doWithdraw(dto.getAmount(), 0.0, "ENVIADO_PAGO_CREDITO_TERCERO", accountSourceDto.get_id(), accountSourceDto.getAccountNumber(), 
                        		dto.getCustomerId(), customerFound); //customerFound is true if the origin account is yours
                        
                        //In this case, since a credit is a debt, it will subtract the amount.
                        doTransaction(dto.getAmount(), 0.0, "RECIBIDO_PAGO_CREDITO_TERCERO", accountDestinationDto.get_id(), accountDestinationDto.getAccountNumber(), 
                        		dto.getCustomerId());
            		} else {
            			throw new Exception("La cantidad a pagar es mayor a la debida ó ya se pago todo el crédito.");
            		}
            		
            	} else {
            		throw new Exception("La cuenta de origen (Pasivos o Débito) o destino (Activos) no son del tipo correspondiente.");
            	}
            	break;
        	}
        } else {
        	throw new Exception("La cuenta de origen no tiene fondos suficientes.");
        }
        
        return response;
	}
}
