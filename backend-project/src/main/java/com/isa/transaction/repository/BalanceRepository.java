package com.isa.transaction.repository;

import com.isa.transaction.entity.Balance;
import com.isa.transaction.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    
    /**
     * Find a balance by client ID and account number
     */
    Optional<Balance> findByClientIdAndAccountNumber(Long clientId, String accountNumber);
    
    /**
     * Find a balance by client and account number
     */
    Optional<Balance> findByClientAndAccountNumber(Client client, String accountNumber);
    
    /**
     * Find all balances for a specific client
     */
    List<Balance> findByClientId(Long clientId);
    
    /**
     * Find all balances for a specific client (using Client entity)
     */
    List<Balance> findByClient(Client client);
    
    /**
     * Find a balance by account number only
     */
    Optional<Balance> findByAccountNumber(String accountNumber);
    
    /**
     * Check if balance exists for client and account
     */
    boolean existsByClientIdAndAccountNumber(Long clientId, String accountNumber);
    
    /**
     * Get total balance for a client across all accounts
     */
    @Query("SELECT COALESCE(SUM(b.currentBalance), 0) FROM Balance b WHERE b.client.id = :clientId")
    java.math.BigDecimal getTotalBalanceByClientId(@Param("clientId") Long clientId);

}