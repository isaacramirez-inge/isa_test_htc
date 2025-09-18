package com.isa.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TransactionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionBackendApplication.class, args);
    }

}