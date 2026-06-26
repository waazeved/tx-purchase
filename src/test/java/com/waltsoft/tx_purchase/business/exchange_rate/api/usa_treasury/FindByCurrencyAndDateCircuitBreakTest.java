package com.waltsoft.tx_purchase.business.exchange_rate.api.usa_treasury;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DisplayName("UsaTreasuryExchangeRateApi.findByCurrencyAndDate circuit break tests")
class FindByCurrencyAndDateCircuitBreakTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();


    @MockitoSpyBean
    private UsaTreasuryExchangeRateApi exchangeRateApi;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    @Qualifier("registerUsaTreasuryExchangeRateCacheManager")
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry
                .circuitBreaker(UsaTreasuryExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME)
                .reset();

        clearExchangeRateCache();
    }


    @Test
    @DisplayName("Should circuit break when exchange rate API failure exceeds limits defined in ExchangeRateResilienceConfig")
    void shouldOpenCircuitWhenApiFailureLimitExceeds() {
        List<UsaTreasuryExchangeRateDto> exchangeRates = List.of();

        int minNumberOfCalls = UsaTreasuryExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_MIN_NUMBER_OF_CALLS;
        int failureRateThreshold = UsaTreasuryExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD;
        int slidingWindowSize = UsaTreasuryExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE;

        int totalCallsNumberBeforeCircuitBreak = slidingWindowSize < minNumberOfCalls
                ? slidingWindowSize + minNumberOfCalls:slidingWindowSize;

        int errorCallsNumber = (int) Math.round(slidingWindowSize * (failureRateThreshold / 100.0));
        int successCallsNumber = totalCallsNumberBeforeCircuitBreak - errorCallsNumber;

        Mockito.doReturn(exchangeRates).when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate
                        (Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE));

        for (int i = 0; i < successCallsNumber; i++) {
            clearExchangeRateCache();
            this.exchangeRateApi.findByCurrencyAndDate(CURRENCY, DATE);
        }

        Mockito.doThrow(new UnavailableExchangeRateApiRuntimeException("API Fail")).when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate
                        (Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE));

        for (int i = 0; i < errorCallsNumber; i++) {
            clearExchangeRateCache();

            Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class, () ->
                    this.exchangeRateApi.findByCurrencyAndDate(CURRENCY, DATE));
        }

        clearExchangeRateCache();

        Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class, () ->
                this.exchangeRateApi.findByCurrencyAndDate(CURRENCY, DATE));

        Mockito.verify(exchangeRateApi, Mockito.times(totalCallsNumberBeforeCircuitBreak))
                .findByCurrencyAndDate(CURRENCY, DATE);
    }

    private void clearExchangeRateCache() {
        Optional.ofNullable(cacheManager.getCache(UsaTreasuryExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME))
                .ifPresent(Cache::clear);
    }
}