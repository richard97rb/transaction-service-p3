package com.microservice.transaction.controllers;

import com.microservice.transaction.entities.dtos.CreateTransactionDto;
import com.microservice.transaction.entities.dtos.CreateTransferDto;
import com.microservice.transaction.entities.dtos.ResponseTransactionDto;
import com.microservice.transaction.entities.dtos.TransactionDto;
import com.microservice.transaction.services.ITransactionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT,
        RequestMethod.DELETE })
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @PostMapping()
    public Mono<ResponseTransactionDto> createTransaction(@Validated @RequestBody CreateTransactionDto dto) throws  Exception{
        return Mono.just(transactionService.createTransaction(dto));
    }

    @GetMapping("accountId/{accountId}")
    public Flux<TransactionDto> getTransactionsByAccontId(@PathVariable String accountId) throws  Exception{
        return Flux.fromIterable(transactionService.getTransactionsByAccountId(accountId));
    }
    
    @PostMapping("transfer")
    public Mono<ResponseTransactionDto> createTransfer(@RequestBody CreateTransferDto dto) throws Exception{
    	return Mono.just(transactionService.createTransfer(dto));
    }
}
