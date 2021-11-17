package com.microservice.transaction.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseTransferDto {
    private String _id;

    private Double amount;
    
    private Double commission;
    
    private String transactionType;

    private String customerId;

    private String accountNumberSource;
    
    private String accountNumberDestination;
}
