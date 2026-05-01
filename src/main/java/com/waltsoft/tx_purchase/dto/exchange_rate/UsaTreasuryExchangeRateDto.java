package com.waltsoft.tx_purchase.dto.exchange_rate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;

import java.time.LocalDate;

public record UsaTreasuryExchangeRateDto(
        @JsonProperty("record_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonProperty("exchange_rate")
        String rate
) implements ExchangeRate {
}


