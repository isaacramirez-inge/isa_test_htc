package com.isa.transaction.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "CLIENT", schema = "TESTHTC")
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @Column(name = "LASTNAME", length = 50, nullable = false)
    private String lastname;
    
    @Column(name = "BIRTHDAY")
    private LocalDate birthday;
    
    @Column(name = "PHONE", length = 20)
    private String phone;
    
    @Column(name = "EMAIL", length = 50)
    private String email;
    
    @Column(name = "ADDRESS", length = 100)
    private String address;
    
    @Column(name = "CLIENT_IDENTIFICATION", length = 50, unique = true, nullable = false)
    private String clientIdentification;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Balance> balances;
    
    // Constructors
    public Client() {
    }
    
    public Client(String name, String lastname, String clientIdentification) {
        this.name = name;
        this.lastname = lastname;
        this.clientIdentification = clientIdentification;
    }
    
    // Getters and setters
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
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getClientIdentification() {
        return clientIdentification;
    }
    
    public void setClientIdentification(String clientIdentification) {
        this.clientIdentification = clientIdentification;
    }
    
    public List<Balance> getBalances() {
        return balances;
    }
    
    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }
    
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", birthday=" + birthday +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", clientIdentification='" + clientIdentification + '\'' +
                '}';
    }
}