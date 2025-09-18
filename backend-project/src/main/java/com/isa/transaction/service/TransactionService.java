package com.isa.transaction.service;

import com.isa.transaction.dto.TransactionRequest;
import com.isa.transaction.dto.TransactionResponse;
import com.isa.transaction.dto.TransactionResultEvent;
import com.isa.transaction.entity.Balance;
import com.isa.transaction.entity.BalanceTransaction;
import com.isa.transaction.entity.Client;
import com.isa.transaction.exception.TransactionException;
import com.isa.transaction.repository.BalanceRepository;
import com.isa.transaction.repository.BalanceTransactionRepository;
import com.isa.transaction.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private BalanceRepository balanceRepository;
    
    @Autowired
    private BalanceTransactionRepository balanceTransactionRepository;
    
    @Autowired
    private KafkaProducerService kafkaProducerService;
    
    /**
     * Process a transaction request
     * This method handles the complete transaction flow:
     * 1. Find the client
     * 2. Find or create the balance/account
     * 3. Validate the transaction (for debits)
     * 4. Update the balance
     * 5. Save the transaction record
     * 6. Send result to Kafka
     */
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        String transactionId = generateTransactionId();
        logger.info("Processing transaction {} for client {} account {} amount {}", 
                   transactionId, request.getClientIdentification(), request.getAccountNumber(), request.getAmount());
        
        try {
            // Step 1: Find or create the client
            Client client = findOrCreateClient(request.getClientIdentification());
            
            // Step 2: Find or create the balance
            Balance balance = findOrCreateBalance(client, request.getAccountNumber(), request.getAmount());
            
            // Step 3: Validate transaction (for debits)
            validateTransaction(request, balance);
            
            // Step 4: Update balance
            BigDecimal newBalanceAmount = balance.getCurrentBalance().add(request.getAmount());
            balance.setCurrentBalance(newBalanceAmount);
            balanceRepository.save(balance);
            
            // Step 5: Save transaction record
            BigDecimal balanceBefore = balance.getCurrentBalance().subtract(request.getAmount());
            BalanceTransaction transaction = new BalanceTransaction(
                transactionId,
                client.getId(),
                request.getAccountNumber(),
                request.getAmount(),
                balanceBefore,
                newBalanceAmount
            );
            balanceTransactionRepository.save(transaction);
            
            // Step 6: Send success event to Kafka (async)
            sendSuccessEventAsync(transactionId, client.getId(), request, newBalanceAmount);
            
            logger.info("Transaction {} completed successfully. New balance: {}", transactionId, newBalanceAmount);
            return TransactionResponse.accepted(transactionId);
            
        } catch (TransactionException e) {
            logger.error("Transaction {} failed: {}", transactionId, e.getMessage());
            sendFailureEventAsync(transactionId, null, request, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error processing transaction {}", transactionId, e);
            TransactionException transactionException = new TransactionException("System error processing transaction", "SYSTEM_ERROR", e);
            sendFailureEventAsync(transactionId, null, request, transactionException);
            throw transactionException;
        }
    }
    
    private Client findOrCreateClient(String clientIdentification) {
        return clientRepository.findByClientIdentification(clientIdentification)
                .orElseGet(() -> {
                    logger.info("Creating new client with identification {}", clientIdentification);
                    Client client = new Client();
                    client.setName("N/A");
                    client.setLastname("N/A");
                    client.setClientIdentification(clientIdentification);
                    return clientRepository.save(client);
                });
    }
    
    private Balance findOrCreateBalance(Client client, String accountNumber, BigDecimal initialAmount) {
        return balanceRepository.findByClientAndAccountNumber(client, accountNumber)
                .orElseGet(() -> {
                    logger.info("Creating new account {} for client {}", accountNumber, client.getId());
                    
                    // For account creation, we use the transaction amount as initial balance
                    // But we need to ensure it's not a debit that would create a negative balance
                    BigDecimal initialBalance = initialAmount.compareTo(BigDecimal.ZERO) >= 0 ? initialAmount : BigDecimal.ZERO;
                    
                    Balance newBalance = new Balance(accountNumber, initialBalance, client);
                    return balanceRepository.save(newBalance);
                });
    }
    
    private void validateTransaction(TransactionRequest request, Balance balance) {
        // For debit transactions, check sufficient funds
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal requestedDebitAmount = request.getAmount().abs();
            if (balance.getCurrentBalance().compareTo(requestedDebitAmount) < 0) {
                throw new TransactionException(
                    String.format("Insufficient funds in account %s. Requested: %s, Available: %s", 
                                 request.getAccountNumber(), requestedDebitAmount, balance.getCurrentBalance()),
                    "INSUFFICIENT_FUNDS"
                );
            }
        }
    }
    
    @Async
    private void sendSuccessEventAsync(String transactionId, Long clientId, TransactionRequest request, BigDecimal newBalance) {
        try {
            TransactionResultEvent event = TransactionResultEvent.completed(
                transactionId, 
                clientId, 
                request.getAccountNumber(), 
                request.getAmount(), 
                newBalance
            );
            kafkaProducerService.sendTransactionResult(event);
            logger.debug("Success event sent for transaction {}", transactionId);
        } catch (Exception e) {
            logger.error("Failed to send success event for transaction {}: {}", transactionId, e.getMessage());
            // We don't throw here to avoid rolling back the successful transaction
        }
    }
    
    @Async
    private void sendFailureEventAsync(String transactionId, Long clientId, TransactionRequest request, TransactionException exception) {
        try {
            TransactionResultEvent event;
            switch (exception.getErrorCode()) {
                case "CLIENT_NOT_FOUND":
                    event = TransactionResultEvent.clientNotFound(transactionId, clientId, 
                                                                request.getAccountNumber(), request.getAmount());
                    break;
                case "INSUFFICIENT_FUNDS":
                    event = TransactionResultEvent.insufficientFunds(transactionId, clientId, 
                                                                   request.getAccountNumber(), request.getAmount());
                    break;
                case "VALIDATION_ERROR":
                    event = TransactionResultEvent.validationError(transactionId, clientId, 
                                                                 request.getAccountNumber(), request.getAmount(), exception.getMessage());
                    break;
                default:
                    event = TransactionResultEvent.systemError(transactionId, clientId, 
                                                             request.getAccountNumber(), request.getAmount(), exception.getMessage());
                    break;
            }
            kafkaProducerService.sendTransactionResult(event);
            logger.debug("Failure event sent for transaction {}", transactionId);
        } catch (Exception e) {
            logger.error("Failed to send failure event for transaction {}: {}", transactionId, e.getMessage());
        }
    }
    
    private String generateTransactionId() {
        return "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
    
    // Helper methods for testing and monitoring
    public Balance getBalance(String clientIdentification, String accountNumber) {
        Client client = clientRepository.findByClientIdentification(clientIdentification).orElse(null);
        if (client == null) {
            return null;
        }
        return balanceRepository.findByClientIdAndAccountNumber(client.getId(), accountNumber)
                .orElse(null);
    }
    
    public java.util.List<BalanceTransaction> getTransactionHistory(Long clientId, String accountNumber) {
        return balanceTransactionRepository.findByClientIdAndAccountNumberOrderByCreatedAtDesc(clientId, accountNumber);
    }
}