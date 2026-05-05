package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

class FindExchangeRateValueByCurrencyAndDateCircuitBreakTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();


    @MockitoSpyBean
    private ExchangeRateServiceImpl exchangeRateService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry
                .circuitBreaker(ExchangeRateResilienceConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER)
                .reset();
    }

    @Test
    @DisplayName("Should circuit break when exchange rate API failure exceeds limits defined in ExchangeRateResilienceConfig")
    void shouldOpenCircuitWhenApiFailureLimitExceeds() {
        List<ExchangeRate> exchangeRates = List.of();

        int minNumberOfCalls = ExchangeRateResilienceConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_MIN_NUMBER_OF_CALLS;
        int failureRateThreshold = ExchangeRateResilienceConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD;
        int slidingWindowSize = ExchangeRateResilienceConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE;

        int totalCallsNumberBeforeCircuitBreak = slidingWindowSize < minNumberOfCalls
                ? slidingWindowSize + minNumberOfCalls:slidingWindowSize;

        int errorCallsNumber = (int) Math.round(slidingWindowSize * (failureRateThreshold / 100.0));
        int successCallsNumber = totalCallsNumberBeforeCircuitBreak - errorCallsNumber;

        for (int i = 0; i < successCallsNumber; i++) {

            Mockito.doReturn(exchangeRates).when(exchangeRateService)
                    .findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate
                            (Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE));

            this.exchangeRateService.findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE);
        }

        for (int i = 0; i < errorCallsNumber; i++) {

            Mockito.doThrow(new UnavailableExchangeRateApiRuntimeException("API Fail")).when(exchangeRateService)
                    .findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate
                            (Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE));

            Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class, () ->
                    this.exchangeRateService.findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE));
        }

        Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class, () ->
                this.exchangeRateService.findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE));

        Mockito.verify(exchangeRateService, Mockito.times(totalCallsNumberBeforeCircuitBreak))
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE);
    }
}