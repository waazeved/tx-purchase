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
import java.util.stream.Collectors;

@Service
class ExchangeRateServiceImpl implements ExchangeRateService {

    private final List<ExchangeRateApi> exchangeRateApis;

    @Autowired
    public ExchangeRateServiceImpl(List<ExchangeRateApi> exchangeRateApis) {
        this.exchangeRateApis = exchangeRateApis.stream()
                .sorted(Comparator.comparingInt(ExchangeRateApi::getPriority))
                .collect(Collectors.toList());
    }

    public Optional<BigDecimal> findExchangeRateValueByCurrencyAndDate(String currency, LocalDate date) {

        ConcurrentHashMap<Integer, Optional<BigDecimal>> exchangeRateValueMap = new ConcurrentHashMap<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (ExchangeRateApi api : exchangeRateApis) {
                Future<?> future = executor.submit(() -> {
                    Optional<BigDecimal> result;
                    try {
                        result = api.findExchangeRateValueByCurrencyAndDate(currency, date);
                    } catch (UnavailableExchangeRateApiRuntimeException e) {
                        result = null;
                    }
                    exchangeRateValueMap.put(api.getPriority(), result);
                });
                futures.add(future);
            }

            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    throw new ExchangeRateApiRuntimeException("An unexpected error occurred while fetching the exchange rate.", cause);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExchangeRateApiRuntimeException("The thread was interrupted while fetching the exchange rate.", e);
        }

        boolean allApisUnavailable = exchangeRateValueMap.values().stream().allMatch(Objects::isNull);
        if (allApisUnavailable) {
            throw new UnavailableExchangeRateApiRuntimeException("All exchange rate APIs were unavailable.");
        }

        return exchangeRateValueMap.entrySet().stream()
                .filter(entry -> entry.getValue()!=null)
                .filter(entry -> entry.getValue().isPresent())
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(Optional.empty());
    }
}
