package com.isa.transaction.repository;

import com.isa.transaction.entity.DeadLetterMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessage, Long> {
    
    /**
     * Find all dead letter messages for a specific topic
     */
    List<DeadLetterMessage> findByTopicOrderByCreatedDateDesc(String topic);
    
    /**
     * Find dead letter messages by topic with pagination
     */
    Page<DeadLetterMessage> findByTopicOrderByCreatedDateDesc(String topic, Pageable pageable);
    
    /**
     * Find dead letter messages within a date range
     */
    List<DeadLetterMessage> findByCreatedDateBetweenOrderByCreatedDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find dead letter messages by topic and date range
     */
    List<DeadLetterMessage> findByTopicAndCreatedDateBetweenOrderByCreatedDateDesc(
            String topic, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count dead letter messages by topic
     */
    long countByTopic(String topic);
    
    /**
     * Find recent dead letter messages (last N days)
     */
    @Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.createdDate >= :fromDate ORDER BY dlm.createdDate DESC")
    List<DeadLetterMessage> findRecentMessages(@Param("fromDate") LocalDateTime fromDate);
    
    /**
     * Find all dead letter messages ordered by creation date (most recent first)
     */
    List<DeadLetterMessage> findAllByOrderByCreatedDateDesc();
    
    /**
     * Find all dead letter messages with pagination ordered by creation date
     */
    Page<DeadLetterMessage> findAllByOrderByCreatedDateDesc(Pageable pageable);
}