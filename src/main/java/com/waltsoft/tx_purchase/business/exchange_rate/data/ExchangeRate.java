package com.waltsoft.tx_purchase.business.exchange_rate.data;

import java.time.LocalDate;

public interface ExchangeRate {

    LocalDate date();

    String rate();

}
