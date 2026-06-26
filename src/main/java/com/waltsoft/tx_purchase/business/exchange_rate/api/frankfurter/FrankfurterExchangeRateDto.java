package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import java.math.BigDecimal;
import java.time.LocalDate;

record FrankfurterExchangeRateDto(
        LocalDate date,
        BigDecimal value
) {
}
