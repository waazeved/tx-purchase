package com.waltsoft.tx_purchase.business.exchange_rate.api.usa_treasury;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;

import java.time.LocalDate;

record UsaTreasuryExchangeRateDto(
        @JsonProperty("record_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonProperty("exchange_rate")
        String value
) implements ExchangeRate {
}


