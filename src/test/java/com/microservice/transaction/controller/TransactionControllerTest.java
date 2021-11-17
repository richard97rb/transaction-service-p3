package com.microservice.transaction.controller;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.microservice.transaction.controllers.TransactionController;
import com.microservice.transaction.entities.dtos.CreateTransactionDto;
import com.microservice.transaction.entities.dtos.CreateTransferDto;
import com.microservice.transaction.entities.dtos.ResponseTransactionDto;
import com.microservice.transaction.entities.dtos.TransactionDto;
import com.microservice.transaction.services.ITransactionService;

@ExtendWith(SpringExtension.class)
@WebFluxTest(TransactionController.class)
public class TransactionControllerTest {

	@Autowired
	WebTestClient webTestClient;
	
	@MockBean
	private ITransactionService transactionService;
	
	public static final ModelMapper modelMapper = new ModelMapper();
	
	@Test
	public void getTransactionsByAccountIdTest() throws Exception {
		
		List<TransactionDto> response = Arrays.asList(TransactionDto.builder()
									.amount(10.0)
									.commission(0.0)
									.transactionType("DEPOSITO")
									.accountId("618aaefeb3a82944b2c659c7")
									.transactionDate(LocalDateTime.now())
									.build());
		
		when(transactionService.getTransactionsByAccountId("618aaefeb3a82944b2c659c7")).thenReturn(response);
		
		webTestClient.get()
			.uri("/api/transactions/accountId/618aaefeb3a82944b2c659c7")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(TransactionDto.class);
		
	}
	
	@Test
	public void createTransactionTest() throws Exception {
		
		//Body Request
		CreateTransactionDto transaction = CreateTransactionDto.builder()
												.amount(10.0)
												.transactionType("DEPOSITO")
												.customerId("6189b0e24dedba3b28cc1138")
												.accountNumber("73582232028410884175")
												.build();
		
		//Response
		ResponseTransactionDto response = modelMapper.map(transaction, ResponseTransactionDto.class);
		response.set_id(new ObjectId().toString());
		response.setCommission(0.2);
		
		when(transactionService.createTransaction(transaction)).thenReturn(response);
		
		webTestClient.post()
			.uri("/api/transactions/")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(transaction)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ResponseTransactionDto.class);					
		
	}
	
	@Test
	public void createTransfer() throws Exception {
		
		//Body Request
		CreateTransferDto transfer = CreateTransferDto.builder()
												.amount(5.0)
												.transactionType("CREDITO_TERCEROS")
												.customerId("6189b0e24dedba3b28cc1138")
												.accountNumberSource("73582232028410884175")
												.accountNumberDestination("97461348314184025877")
												.build();
		
		//Response
		ResponseTransactionDto response = ResponseTransactionDto.builder()
											._id(new ObjectId().toString())
											.amount(transfer.getAmount())
											.commission(0.0)
											.transactionType(transfer.getTransactionType())
											.customerId(transfer.getCustomerId())
											.accountNumber(transfer.getAccountNumberSource())
											.build();
		
		
		when(transactionService.createTransfer(transfer)).thenReturn(response);
		
		webTestClient.post()
			.uri("/api/transactions/transfer")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(transfer)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ResponseTransactionDto.class);					
		
	}
	
}
