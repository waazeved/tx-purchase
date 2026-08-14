package com.waltsoft.tx_purchase.business.exchange_rate.exception;

public class ExchangeRateApiRuntimeException extends RuntimeException {

    public ExchangeRateApiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRateApiRuntimeException(String message) {
        super(message);
    }
}
