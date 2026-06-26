package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.api.ExchangeRateApi;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.ExchangeRateApiRuntimeException;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
class ExchangeRateServiceImpl implements ExchangeRateService {

    private final List<ExchangeRateApi> exchangeRateApis;

    @Autowired
    public ExchangeRateServiceImpl(List<ExchangeRateApi> exchangeRateApis) {
        this.exchangeRateApis = exchangeRateApis.stream()
                .sorted(Comparator.comparingInt(ExchangeRateApi::getPriority))
                .toList();
    }

    @Override
    public Optional<BigDecimal> findByCurrencyAndDate(String currency, LocalDate date) {

        ConcurrentHashMap<Integer, Optional<BigDecimal>> exchangeRateValueMap = new ConcurrentHashMap<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (ExchangeRateApi api : exchangeRateApis) {
                Future<?> future = executor.submit(() -> {
                    try {
                        Optional<BigDecimal> result = api.findByCurrencyAndDate(currency, date);
                        exchangeRateValueMap.put(api.getPriority(), result);
                    } catch (UnavailableExchangeRateApiRuntimeException e) {
                        //Do nothing
                    }
                });
                futures.add(future);
            }

            waitFinishFutures(futures);
        }

        if (exchangeRateValueMap.isEmpty()) {
            throw new UnavailableExchangeRateApiRuntimeException("All exchange rate APIs were unavailable.");
        }

        return exchangeRateValueMap.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .min(Map.Entry.comparingByKey()).flatMap(Map.Entry::getValue);
    }

    private void waitFinishFutures(List<Future<?>> futures) {
        try {
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new ExchangeRateApiRuntimeException("An unexpected error occurred while fetching the exchange rate.", cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExchangeRateApiRuntimeException("The thread was interrupted while fetching the exchange rate.", e);
        }
    }
}
