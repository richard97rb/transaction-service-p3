package com.microservice.transaction.services;

import com.microservice.transaction.entities.dtos.CreateTransactionDto;
import com.microservice.transaction.entities.dtos.CreateTransferDto;
import com.microservice.transaction.entities.dtos.ResponseTransactionDto;
import com.microservice.transaction.entities.dtos.ResponseTransferDto;
import com.microservice.transaction.entities.dtos.TransactionDto;

import java.util.List;

public interface ITransactionService{
    ResponseTransactionDto createTransaction(CreateTransactionDto dto) throws  Exception;
    ResponseTransactionDto createTransfer(CreateTransferDto dto) throws Exception;
    List<TransactionDto> getTransactionsByAccountId(String id) throws  Exception;
}
