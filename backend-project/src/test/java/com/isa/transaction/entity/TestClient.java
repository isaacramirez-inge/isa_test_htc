package com.isa.transaction.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad Client simplificada para pruebas sin schema
 */
@Entity
@Table(name = "client")
public class TestClient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String lastname;
    
    @Column(name = "client_identification", nullable = false, unique = true, length = 50)
    private String clientIdentification;
    
    @Column(nullable = false, length = 150)
    private String email;
    
    @Column(nullable = false)
    private LocalDate birthday;
    
    // Constructors
    public TestClient() {}
    
    public TestClient(String name, String lastname, String clientIdentification, String email, LocalDate birthday) {
        this.name = name;
        this.lastname = lastname;
        this.clientIdentification = clientIdentification;
        this.email = email;
        this.birthday = birthday;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLastname() {
        return lastname;
    }
    
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    public String getClientIdentification() {
        return clientIdentification;
    }
    
    public void setClientIdentification(String clientIdentification) {
        this.clientIdentification = clientIdentification;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}