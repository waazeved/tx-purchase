package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

record FrankfurterExchangeRateApiResponseDto(
        BigDecimal amount,
        String base,
        LocalDate date,
        Map<String, BigDecimal> rates
) {
}
