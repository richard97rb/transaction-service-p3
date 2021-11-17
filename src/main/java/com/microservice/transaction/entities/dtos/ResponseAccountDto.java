package com.microservice.transaction.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseAccountDto {

    private String _id;

    private Double balance;

    private String accountNumber;

    private List<String> customersIds;
    
    private List<String> accountsIds;

    private AccountTypeDto accountType;
}
