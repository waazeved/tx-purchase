package com.waltsoft.tx_purchase.dto.purchase;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseInsertDto(
        String description,
        LocalDate dateTime,
        @NotNull(message = "Purchase amount is required")
        @DecimalMin(value = "0.01", message = "Purchase amount must be at least 0.01")
        BigDecimal amount
) implements Serializable {
}