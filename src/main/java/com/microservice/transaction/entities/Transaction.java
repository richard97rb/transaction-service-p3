package com.microservice.transaction.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    ObjectId _id;

    Double amount;
    
    Double commission;

    @Field("transaction_type")
    String transactionType;

    @Field("account_id")
    ObjectId accountId;
    
    @Field("transaction_date")
    LocalDateTime transactionDate;


}
