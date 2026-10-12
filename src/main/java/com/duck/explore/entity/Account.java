package com.duck.explore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account")
@SuppressWarnings("NullAway.Init")
public class Account {

    @Id
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private String accountName;

    private BigDecimal balance;

    private String accountType;

    private Boolean isActive;

    private Instant createdAt;
}
