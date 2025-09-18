package com.isa.transaction.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DEAD_LETTER_MESSAGES", schema = "TESTHTC")
public class DeadLetterMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "TOPIC", length = 100, nullable = false)
    private String topic;
    
    @Column(name = "MESSAGE_PAYLOAD", columnDefinition = "TEXT", nullable = false)
    private String messagePayload;
    
    @Column(name = "ERROR", columnDefinition = "TEXT")
    private String error;
    
    @Column(name = "CREATEDDATE", nullable = false)
    private LocalDateTime createdDate;
    
    // Constructors
    public DeadLetterMessage() {
    }
    
    public DeadLetterMessage(String topic, String messagePayload, String error) {
        this.topic = topic;
        this.messagePayload = messagePayload;
        this.error = error;
        this.createdDate = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getMessagePayload() {
        return messagePayload;
    }
    
    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    @Override
    public String toString() {
        return "DeadLetterMessage{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", messagePayload='" + messagePayload + '\'' +
                ", error='" + error + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}