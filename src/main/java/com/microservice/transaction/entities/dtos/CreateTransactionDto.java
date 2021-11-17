package com.microservice.transaction.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTransactionDto {

    private Double amount;

    private String transactionType;

    private String customerId;

    private String accountNumber;
}
