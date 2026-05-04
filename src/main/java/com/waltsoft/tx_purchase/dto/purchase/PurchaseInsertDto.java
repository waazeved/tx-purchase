package com.waltsoft.tx_purchase.dto.purchase;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseInsertDto(
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Date time is required")
        LocalDate dateTime,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount
) implements Serializable {
}