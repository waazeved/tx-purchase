package com.waltsoft.tx_purchase.business.exchange_rate.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateApi {

    Optional<BigDecimal> findByCurrencyAndDate(String currency, LocalDate date);

    Integer getPriority();

}
