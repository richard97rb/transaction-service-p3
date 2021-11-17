package com.microservice.transaction.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountTypeDto {
    private String type;

    private Double commissions;

    private Integer maxTransactionsPerMonth;

    private String productType;
}
