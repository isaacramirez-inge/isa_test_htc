package com.isa.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object for a transaction processing request")
public class TransactionResponse {
    
    @JsonProperty("transactionId")
    @Schema(description = "Unique identifier for tracking the transaction", example = "txn_1234567890")
    private String transactionId;
    
    @JsonProperty("status")
    @Schema(description = "Current status of the transaction", example = "IN_PROCESS")
    private String status;
    
    @JsonProperty("message")
    @Schema(description = "Human-readable message about the transaction", example = "Transaction is being processed")
    private String message;
    
    // Constructors
    public TransactionResponse() {
    }
    
    public TransactionResponse(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }
    
    // Static factory methods for common responses
    public static TransactionResponse processing(String transactionId) {
        return new TransactionResponse(transactionId, "IN_PROCESS", "Transaction is being processed");
    }
    
    public static TransactionResponse accepted(String transactionId) {
        return new TransactionResponse(transactionId, "ACCEPTED", "Transaction has been accepted and is being processed");
    }
    
    public static TransactionResponse processing(String transactionId, String customMessage) {
        return new TransactionResponse(transactionId, "IN_PROCESS", customMessage);
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}