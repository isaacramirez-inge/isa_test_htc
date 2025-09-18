package com.isa.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request object for processing a transaction")
public class TransactionRequest {
    
    @NotBlank(message = "Client identification is required")
    @Size(min = 1, max = 50, message = "Client identification must be between 1 and 50 characters")
    @JsonProperty("clientIdentification")
    @Schema(description = "The identification of the client", example = "12345678")
    private String clientIdentification;
    
    @NotBlank(message = "Account number is required")
    @Size(min = 5, max = 25, message = "Account number must be between 5 and 25 characters")
    @JsonProperty("accountNumber")
    @Schema(description = "The account number", example = "ACC-123456")
    private String accountNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "-10000.00", message = "Amount cannot be less than -10,000")
    @DecimalMax(value = "10000.00", message = "Amount cannot be more than 10,000")
    @Digits(integer = 5, fraction = 2, message = "Amount must have at most 5 integer digits and 2 decimal places")
    @JsonProperty("amount")
    @Schema(description = "Transaction amount (positive for credit, negative for debit)", example = "100.50")
    private BigDecimal amount;
    
    // Constructors
    public TransactionRequest() {
    }
    
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
    
    // Helper methods
    public boolean isCredit() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isDebit() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
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