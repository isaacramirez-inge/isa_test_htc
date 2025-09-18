package com.isa.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

enum TransactionType {
    CREDIT, DEBIT
}

@Entity
@Table(name = "BALANCE_TRANSACTION", schema = "TESTHTC")
public class BalanceTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "TRANSACTION_ID", length = 50, nullable = false, unique = true)
    private String transactionId;
    
    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;
    
    @Column(name = "ACCOUNT_NUMBER", length = 10, nullable = false)
    private String accountNumber;
    
    @Column(name = "AMOUNT", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "BALANCE_BEFORE", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceBefore;
    
    @Column(name = "BALANCE_AFTER", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceAfter;
    
    @Column(name = "TRANSACTION_TYPE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Column(name = "CREATED_AT", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    // Referencia a Client para conveniencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENT_ID", insertable = false, updatable = false)
    private Client client;
    
    // Constructores
    public BalanceTransaction() {
    }
    
    public BalanceTransaction(String transactionId, Long clientId, String accountNumber, 
                            BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.transactionType = amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.CREDIT : TransactionType.DEBIT;
        this.createdAt = LocalDateTime.now();
    }
    
    public BalanceTransaction(BigDecimal amount2, Balance balance) {
        this.amount = amount2;
        this.balanceBefore = balance.getCurrentBalance();
        this.balanceAfter = balance.getCurrentBalance().add(amount2);
        this.transactionType = amount2.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.CREDIT : TransactionType.DEBIT;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }
    
    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    // Helper methods
    public boolean isCredit() {
        return transactionType == TransactionType.CREDIT;
    }
    
    public boolean isDebit() {
        return transactionType == TransactionType.DEBIT;
    }
    
    @Override
    public String toString() {
        return "BalanceTransaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", clientId=" + clientId +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", transactionType=" + transactionType +
                ", createdAt=" + createdAt +
                '}';
    }
}