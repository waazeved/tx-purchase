package com.waltsoft.tx_purchase.entity.purchase;

import com.waltsoft.tx_purchase.entity.basic.BasicEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "purchase")
public class Purchase extends BasicEntity {

    @Column(name = "description", length = 50, nullable = false)
    private String description;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;


    protected Purchase() {
    }

    public Purchase(String description, BigDecimal amount, LocalDate dateTime) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.amount = amount;
        this.date = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Purchase purchase)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(getDescription(), purchase.getDescription()) && Objects.equals(getDate(),
                purchase.getDate()) && Objects.equals(
                getAmount(),
                purchase.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDescription(), getDate(), getAmount());
    }

    @Override
    public String toString() {
        return "Purchase{" + "description='" + description + '\'' + ", dateTime=" + date + ", amount=" + amount + ", id=" + id + '}';
    }
}