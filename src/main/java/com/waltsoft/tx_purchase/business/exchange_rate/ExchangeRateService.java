package com.waltsoft.tx_purchase.business.exchange_rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateService {

    Optional<BigDecimal> findExchangeRateValueByCurrencyAndDate(String currency, LocalDate date);
}
