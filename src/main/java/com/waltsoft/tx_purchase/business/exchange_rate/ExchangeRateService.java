package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataException;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateService {
    BigDecimal convertAmountByCurrencyAndDate(BigDecimal amount, String currency, LocalDate date) throws NoExchangeRateDataException;
}
