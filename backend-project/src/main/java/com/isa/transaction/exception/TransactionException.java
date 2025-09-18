package com.isa.transaction.exception;

public class TransactionException extends RuntimeException {
    
    private final String errorCode;
    
    public TransactionException(String message) {
        super(message);
        this.errorCode = "TRANSACTION_ERROR";
    }
    
    public TransactionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TRANSACTION_ERROR";
    }
    
    public TransactionException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

class ClientNotFoundException extends TransactionException {
    public ClientNotFoundException(Long clientId) {
        super("Client with ID " + clientId + " not found", "CLIENT_NOT_FOUND");
    }
}

class InsufficientFundsException extends TransactionException {
    public InsufficientFundsException(String account, java.math.BigDecimal requestedAmount, java.math.BigDecimal availableBalance) {
        super(String.format("Insufficient funds in account %s. Requested: %s, Available: %s", 
              account, requestedAmount, availableBalance), "INSUFFICIENT_FUNDS");
    }
}

class AccountCreationException extends TransactionException {
    public AccountCreationException(String message) {
        super(message, "ACCOUNT_CREATION_ERROR");
    }
    
    public AccountCreationException(String message, Throwable cause) {
        super(message, "ACCOUNT_CREATION_ERROR", cause);
    }
}