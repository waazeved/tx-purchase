package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

record FrankfurterExchangeRateApiResponseDto(
        BigDecimal amount,
        String base,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("end_date") LocalDate endDate,
        Map<LocalDate, Map<String, BigDecimal>> rates
) {
}
