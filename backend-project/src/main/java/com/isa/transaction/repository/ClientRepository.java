package com.isa.transaction.repository;

import com.isa.transaction.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * Find a client by name and lastname
     */
    Optional<Client> findByNameAndLastname(String name, String lastname);
    
    /**
     * Find a client by email
     */
    Optional<Client> findByEmail(String email);
    
    /**
     * Find a client by client identification
     */
    Optional<Client> findByClientIdentification(String clientIdentification);
    
    /**
     * Check if client exists by ID
     */
    boolean existsById(Long id);
    
    /**
     * Find clients by name (partial match, case insensitive)
     */
    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<Client> findByNameContainingIgnoreCase(@Param("name") String name);
}