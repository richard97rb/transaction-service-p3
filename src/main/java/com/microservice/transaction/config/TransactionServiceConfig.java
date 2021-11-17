package com.microservice.transaction.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource({"classpath:application.properties"})
public class TransactionServiceConfig {

    @Value("${accountservice.url}")
    private String accountServiceUrl;
}
