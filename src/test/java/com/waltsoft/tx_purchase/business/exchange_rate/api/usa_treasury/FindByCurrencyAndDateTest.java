package com.waltsoft.tx_purchase.business.exchange_rate.api.usa_treasury;

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

@DisplayName("UsaTreasuryExchangeRateApi.findByCurrencyAndDate tests")
class FindByCurrencyAndDateTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    @Qualifier("registerUsaTreasuryExchangeRateCacheManager")
    private CacheManager cacheManager;

    private UsaTreasuryExchangeRateApi exchangeRateApi;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry
                .circuitBreaker(UsaTreasuryExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME)
                .reset();

        Cache cache = cacheManager.getCache(UsaTreasuryExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME);

        if (cache!=null) {
            cache.clear();
        }

        WebClient webClient = Mockito.mock(WebClient.class);
        exchangeRateApi = Mockito.spy(new UsaTreasuryExchangeRateApi(
                webClient,
                Duration.ofSeconds(1),
                1,
                Duration.ofSeconds(1)
        ));
    }

    @Test
    @DisplayName("Should find exchange rates and return the most recent exchange rate when there are multiple results")
    void shouldReturnMostRecentExchangeRate() throws NoExchangeRateDataRuntimeException {
        LocalDate targetDate = LocalDate.of(2026, 5, 10);
        LocalDate startDate = targetDate.minusMonths(UsaTreasuryExchangeRateApi.MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS);

        LocalDate date1 = targetDate.minusMonths(1);
        LocalDate date2 = date1.minusDays(1);
        LocalDate date3 = date2.minusDays(1);

        String rate1 = "4.50";
        String rate2 = "5.00";
        String rate3 = "3.25";

        UsaTreasuryExchangeRateDto exchangeRate1 = new UsaTreasuryExchangeRateDto(date1, rate1);
        UsaTreasuryExchangeRateDto exchangeRate2 = new UsaTreasuryExchangeRateDto(date2, rate2);
        UsaTreasuryExchangeRateDto exchangeRate3 = new UsaTreasuryExchangeRateDto(date3, rate3);

        List<UsaTreasuryExchangeRateDto> exchangeRates = List.of(exchangeRate1, exchangeRate2, exchangeRate3);

        Mockito.doReturn(exchangeRates)
                .when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        CURRENCY,
                        startDate,
                        targetDate
                );

        BigDecimal expectedExchangeRateValue = new BigDecimal(rate1);
        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateApi.findByCurrencyAndDate(CURRENCY, targetDate);

        Assertions.assertTrue(exchangeRateValueOptional.isPresent());
        Assertions.assertEquals(expectedExchangeRateValue, exchangeRateValueOptional.get());
    }

    @Test
    @DisplayName("Should returns empty when api returns nothing")
    void shouldReturnEmptyWhenApiReturnsNothing() throws NoExchangeRateDataRuntimeException {
        LocalDate targetDate = LocalDate.of(2026, 5, 10);
        LocalDate startDate = targetDate.minusMonths(UsaTreasuryExchangeRateApi.MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS);


        List<UsaTreasuryExchangeRateDto> exchangeRates = List.of();

        Mockito.doReturn(exchangeRates)
                .when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        CURRENCY,
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
                        ArgumentMatchers.eq(CURRENCY),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(DATE)
                );

        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateApi
                .findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(exchangeRateValueOptional.isEmpty());
    }
}