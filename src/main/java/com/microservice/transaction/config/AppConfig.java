package com.microservice.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

enum TransactionTypeEnum{
    DEPOSITO("DEPOSITO"),
    RETIRO("RETIRO"),
    PAGO("PAGO"),
    TRANSFERENCIA_CUENTA("TRANSFERENCIA_CUENTA"),
	CREDITO_TERCEROS("CREDITO_TERCEROS");

    private String type;

    TransactionTypeEnum(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }
}

@Component
public class AppConfig {

    public Optional<String> getTransactionTypeByName(String name){
        TransactionTypeEnum[] transactionTypes = TransactionTypeEnum.values();
        Optional<TransactionTypeEnum> typeEnum = Arrays.stream(transactionTypes)
                .filter(x->x.getType().equals(name))
                .findFirst();
        return Optional.ofNullable(typeEnum.get().getType());
    }
}
