package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;

import java.time.LocalDate;

public interface ExchangeRateService {

    ExchangeRate findExchangeRateByCurrencyAndDate(String currency, LocalDate date);
}
