package com.isa.transaction.frontend.dto;

import java.math.BigDecimal;

/**
 * DTO for sending transaction requests to the backend API
 */
public class TransactionRequest {
    
    private String clientIdentification;
    private String accountNumber;
    private BigDecimal amount;
    
    // Default constructor
    public TransactionRequest() {
    }
    
    // Constructor with parameters
    public TransactionRequest(String clientIdentification, String accountNumber, BigDecimal amount) {
        this.clientIdentification = clientIdentification;
        this.accountNumber = accountNumber;
        this.amount = amount;
    }
    
    // Getters and setters
    public String getClientIdentification() {
        return clientIdentification;
    }
    
    public void setClientIdentification(String clientIdentification) {
        this.clientIdentification = clientIdentification;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return "TransactionRequest{" +
                "clientIdentification='" + clientIdentification + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                '}';
    }
}