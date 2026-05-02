package com.waltsoft.tx_purchase.business.exchange_rate;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateService {
    BigDecimal convertAmountByCurrencyAndDate(BigDecimal amount, String currency, LocalDate date);
}
