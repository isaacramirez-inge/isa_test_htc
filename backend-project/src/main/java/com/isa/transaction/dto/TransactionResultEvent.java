package com.isa.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Event object sent to Kafka with transaction results")
public class TransactionResultEvent {
    
    @JsonProperty("transactionId")
    @Schema(description = "Unique identifier for the transaction", example = "txn_1234567890")
    private String transactionId;
    
    @JsonProperty("clientId")
    @Schema(description = "The ID of the client", example = "12345")
    private Long clientId;
    
    @JsonProperty("accountNumber")
    @Schema(description = "The account number", example = "ACC-1234567")
    private String accountNumber;
    
    @JsonProperty("amount")
    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;
    
    @JsonProperty("finalStatus")
    @Schema(description = "Final status of the transaction", 
            example = "COMPLETED", 
            allowableValues = {"COMPLETED", "FAILED_INSUFFICIENT_FUNDS", "FAILED_CLIENT_NOT_FOUND", "FAILED_VALIDATION_ERROR", "FAILED_SYSTEM_ERROR"})
    private String finalStatus;
    
    @JsonProperty("errorMessage")
    @Schema(description = "Error message if transaction failed", example = "Insufficient funds")
    private String errorMessage;
    
    @JsonProperty("completedAt")
    @Schema(description = "Timestamp when transaction was completed", example = "2023-09-17T12:30:45")
    private LocalDateTime completedAt;
    
    @JsonProperty("newBalance")
    @Schema(description = "New balance after transaction (if successful)", example = "500.75")
    private BigDecimal newBalance;
    
    // Constructors
    public TransactionResultEvent() {
    }
    
    public TransactionResultEvent(String transactionId, Long clientId, String accountNumber, 
                                 BigDecimal amount, String finalStatus, String errorMessage, 
                                 LocalDateTime completedAt, BigDecimal newBalance) {
        this.transactionId = transactionId;
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.finalStatus = finalStatus;
        this.errorMessage = errorMessage;
        this.completedAt = completedAt;
        this.newBalance = newBalance;
    }
    
    // Static factory methods for common events
    public static TransactionResultEvent completed(String transactionId, Long clientId, String accountNumber, 
                                                  BigDecimal amount, BigDecimal newBalance) {
        return new TransactionResultEvent(transactionId, clientId, accountNumber, amount, 
                                        "COMPLETED", null, LocalDateTime.now(), newBalance);
    }
    
    public static TransactionResultEvent insufficientFunds(String transactionId, Long clientId, 
                                                          String accountNumber, BigDecimal amount) {
        return new TransactionResultEvent(transactionId, clientId, accountNumber, amount, 
                                        "FAILED_INSUFFICIENT_FUNDS", "Insufficient funds for this transaction", 
                                        LocalDateTime.now(), null);
    }
    
    public static TransactionResultEvent clientNotFound(String transactionId, Long clientId, 
                                                       String accountNumber, BigDecimal amount) {
        return new TransactionResultEvent(transactionId, clientId, accountNumber, amount, 
                                        "FAILED_CLIENT_NOT_FOUND", "Client not found", 
                                        LocalDateTime.now(), null);
    }
    
    public static TransactionResultEvent validationError(String transactionId, Long clientId, 
                                                        String accountNumber, BigDecimal amount, String error) {
        return new TransactionResultEvent(transactionId, clientId, accountNumber, amount, 
                                        "FAILED_VALIDATION_ERROR", error, 
                                        LocalDateTime.now(), null);
    }
    
    public static TransactionResultEvent systemError(String transactionId, Long clientId, 
                                                    String accountNumber, BigDecimal amount, String error) {
        return new TransactionResultEvent(transactionId, clientId, accountNumber, amount, 
                                        "FAILED_SYSTEM_ERROR", error, 
                                        LocalDateTime.now(), null);
    }
    
    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Long getClientId() {
        return clientId;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
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
    
    public String getFinalStatus() {
        return finalStatus;
    }
    
    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public BigDecimal getNewBalance() {
        return newBalance;
    }
    
    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }
    
    // Helper methods
    public boolean isSuccess() {
        return "COMPLETED".equals(finalStatus);
    }
    
    public boolean isFailure() {
        return finalStatus != null && finalStatus.startsWith("FAILED_");
    }
    
    @Override
    public String toString() {
        return "TransactionResultEvent{" +
                "transactionId='" + transactionId + '\'' +
                ", clientId=" + clientId +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", finalStatus='" + finalStatus + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", completedAt=" + completedAt +
                ", newBalance=" + newBalance +
                '}';
    }
}