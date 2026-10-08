package com.duck.explore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tbl_account")
@SuppressWarnings("NullAway.Init") // Ignores uninitialized field warnings for the whole class
public class Account {

    @Id
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private String accountName;

    private BigDecimal balance;

    private String status;

    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero");
        }

        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }

        this.balance = this.balance.add(amount);
    }
}
