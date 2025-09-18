package com.isa.transaction.frontend.dto;

/**
 * DTO for transaction response data
 */
public class TransactionResponse {
    
    private String transactionId;
    private String status;
    
    // Default constructor
    public TransactionResponse() {
    }
    
    // Constructor with parameters
    public TransactionResponse(String transactionId, String status) {
        this.transactionId = transactionId;
        this.status = status;
    }
    
    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}