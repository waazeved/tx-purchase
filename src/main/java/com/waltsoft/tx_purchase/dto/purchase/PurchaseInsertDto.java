package com.waltsoft.tx_purchase.dto.purchase;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseInsertDto(
        @NotBlank(message = "Description is required")
        @Size(max = MAX_DESCRIPTION_SIZE, message = "Description must be at most 50 characters")
        String description,
        @NotNull(message = "Date time is required")
        LocalDate dateTime,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount
) implements Serializable {
    public static final int MAX_DESCRIPTION_SIZE = 50;
}