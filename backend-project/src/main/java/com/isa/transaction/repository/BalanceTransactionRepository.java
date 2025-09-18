package com.isa.transaction.repository;

import com.isa.transaction.entity.BalanceTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {
    
    /**
     * Find all transactions for a specific client and account
     */
    List<BalanceTransaction> findByClientIdAndAccountNumberOrderByCreatedAtDesc(Long clientId, String accountNumber);
    
    /**
     * Find all transactions for a client and account with pagination
     */
    Page<BalanceTransaction> findByClientIdAndAccountNumberOrderByCreatedAtDesc(Long clientId, String accountNumber, Pageable pageable);
    
    /**
     * Find transactions by client ID
     */
    List<BalanceTransaction> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    /**
     * Find transactions by client ID with pagination
     */
    Page<BalanceTransaction> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    
    /**
     * Find transactions by account number
     */
    List<BalanceTransaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
    
    /**
     * Find transactions within a date range for a specific client and account
     */
    List<BalanceTransaction> findByClientIdAndAccountNumberAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long clientId, String accountNumber, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find credit transactions for a client and account
     */
    @Query("SELECT bt FROM BalanceTransaction bt WHERE bt.clientId = :clientId AND bt.accountNumber = :accountNumber AND bt.transactionType = 'CREDIT' ORDER BY bt.createdAt DESC")
    List<BalanceTransaction> findCreditTransactionsByClientAndAccount(@Param("clientId") Long clientId, @Param("accountNumber") String accountNumber);
    
    /**
     * Find debit transactions for a client and account
     */
    @Query("SELECT bt FROM BalanceTransaction bt WHERE bt.clientId = :clientId AND bt.accountNumber = :accountNumber AND bt.transactionType = 'DEBIT' ORDER BY bt.createdAt DESC")
    List<BalanceTransaction> findDebitTransactionsByClientAndAccount(@Param("clientId") Long clientId, @Param("accountNumber") String accountNumber);
    
    /**
     * Get total credit amount for a client and account
     */
    @Query("SELECT COALESCE(SUM(bt.amount), 0) FROM BalanceTransaction bt WHERE bt.clientId = :clientId AND bt.accountNumber = :accountNumber AND bt.transactionType = 'CREDIT'")
    BigDecimal getTotalCreditsByClientAndAccount(@Param("clientId") Long clientId, @Param("accountNumber") String accountNumber);
    
    /**
     * Get total debit amount for a client and account
     */
    @Query("SELECT COALESCE(SUM(bt.amount), 0) FROM BalanceTransaction bt WHERE bt.clientId = :clientId AND bt.accountNumber = :accountNumber AND bt.transactionType = 'DEBIT'")
    BigDecimal getTotalDebitsByClientAndAccount(@Param("clientId") Long clientId, @Param("accountNumber") String accountNumber);
    
    /**
     * Count transactions for a client and account
     */
    long countByClientIdAndAccountNumber(Long clientId, String accountNumber);
}