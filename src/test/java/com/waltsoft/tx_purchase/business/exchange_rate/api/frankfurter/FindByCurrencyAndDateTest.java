package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataRuntimeException;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@DisplayName("FrankfurterExchangeRateApi.findByCurrencyAndDate tests")
class FindByCurrencyAndDateTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    @Qualifier("registerFrankfurterExchangeRateCacheManager")
    private CacheManager cacheManager;

    private FrankfurterExchangeRateApi exchangeRateApi;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry
                .circuitBreaker(FrankfurterExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME)
                .reset();

        Cache cache = cacheManager.getCache(FrankfurterExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME);

        if (cache!=null) {
            cache.clear();
        }

        WebClient webClient = Mockito.mock(WebClient.class);
        FrankfurterCurrencyCodeConverter currencyCodeConverter = Mockito.mock(FrankfurterCurrencyCodeConverter.class);
        Mockito.when(currencyCodeConverter.convert(CURRENCY)).thenReturn(Optional.of("BRL"));

        exchangeRateApi = Mockito.spy(new FrankfurterExchangeRateApi(
                webClient,
                Duration.ofSeconds(1),
                1,
                Duration.ofSeconds(1),
                currencyCodeConverter
        ));
    }

    @Test
    @DisplayName("Should find exchange rates and return the most recent exchange rate when there are multiple results")
    void shouldReturnMostRecentExchangeRate() throws NoExchangeRateDataRuntimeException {
        LocalDate targetDate = LocalDate.of(2026, 5, 10);
        LocalDate startDate = targetDate.minusMonths(FrankfurterExchangeRateApi.MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS);

        LocalDate date1 = targetDate.minusMonths(1);
        LocalDate date2 = date1.minusDays(1);
        LocalDate date3 = date2.minusDays(1);

        BigDecimal rate1 = new BigDecimal("4.50");
        BigDecimal rate2 = new BigDecimal("5.00");
        BigDecimal rate3 = new BigDecimal("3.25");

        FrankfurterExchangeRateDto exchangeRate1 = new FrankfurterExchangeRateDto(date1, rate1);
        FrankfurterExchangeRateDto exchangeRate2 = new FrankfurterExchangeRateDto(date2, rate2);
        FrankfurterExchangeRateDto exchangeRate3 = new FrankfurterExchangeRateDto(date3, rate3);

        List<FrankfurterExchangeRateDto> exchangeRates = List.of(exchangeRate1, exchangeRate2, exchangeRate3);

        Mockito.doReturn(exchangeRates)
                .when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        "BRL",
                        startDate,
                        targetDate
                );

        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateApi.findByCurrencyAndDate(CURRENCY, targetDate);

        Assertions.assertTrue(exchangeRateValueOptional.isPresent());
        Assertions.assertEquals(rate1, exchangeRateValueOptional.get());
    }

    @Test
    @DisplayName("Should returns empty when api returns nothing")
    void shouldReturnEmptyWhenApiReturnsNothing() throws NoExchangeRateDataRuntimeException {
        LocalDate targetDate = LocalDate.of(2026, 5, 10);
        LocalDate startDate = targetDate.minusMonths(FrankfurterExchangeRateApi.MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS);


        List<FrankfurterExchangeRateDto> exchangeRates = List.of();

        Mockito.doReturn(exchangeRates)
                .when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        "BRL",
                        startDate,
                        targetDate
                );

        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateApi.findByCurrencyAndDate(CURRENCY, targetDate);

        Assertions.assertTrue(exchangeRateValueOptional.isEmpty());
    }

    @Test
    @DisplayName("Should return empty Optional when there is no exchange rate for date")
    void shouldThrowExceptionWhenNoRatesFound() {

        Mockito.doReturn(Collections.emptyList())
                .when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        ArgumentMatchers.eq("BRL"),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(DATE)
                );

        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateApi
                .findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(exchangeRateValueOptional.isEmpty());
    }
}