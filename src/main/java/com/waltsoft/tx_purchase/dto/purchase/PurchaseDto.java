package com.waltsoft.tx_purchase.dto.purchase;

import com.waltsoft.tx_purchase.entity.purchase.Purchase;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseDto(
        UUID id,
        String description,
        LocalDate date,
        BigDecimal amount
) implements Serializable {

    public PurchaseDto(Purchase purchase) {
        this(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getDate(),
                purchase.getAmount()
        );
    }
}