package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DisplayName("FrankfurterExchangeRateApi.findByCurrencyAndDate cache tests")
class FindByCurrencyAndDateCacheTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();

    @MockitoSpyBean
    private FrankfurterExchangeRateApi exchangeRateApi;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    @Qualifier("registerFrankfurterExchangeRateCacheManager")
    private CacheManager cacheManager;

    @Autowired
    private FrankfurterCurrencyCodeConverter currencyCodeConverter;

    @BeforeEach
    void setUp() {
        
        circuitBreakerRegistry
                .circuitBreaker(FrankfurterExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME)
                .reset();

        Cache cache = cacheManager.getCache(FrankfurterExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME);

        if (cache!=null) {
            cache.clear();
        }

        Mockito.when(currencyCodeConverter.convert(CURRENCY)).thenReturn(Optional.of("BRL"));
    }

    @Test
    @DisplayName("Should use cache and not execute FrankfurterExchangeRateApi.findExchangeRateValueByCurrencyAndDate")
    void shouldCacheExchangeRateResult() {
        BigDecimal exchangeRateValue = new BigDecimal("5.50");

        FrankfurterExchangeRateDto mockRate = new FrankfurterExchangeRateDto(DATE, exchangeRateValue);
        List<FrankfurterExchangeRateDto> exchangeRates = List.of(mockRate);

        Mockito.doReturn(exchangeRates).when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        Mockito.eq("BRL"), Mockito.any(), Mockito.eq(DATE)
                );

        BigDecimal exchangeRateValue1 = exchangeRateApi
                .findByCurrencyAndDate(CURRENCY, DATE)
                .orElseThrow();

        BigDecimal exchangeRateValue2 = exchangeRateApi
                .findByCurrencyAndDate(CURRENCY, DATE)
                .orElseThrow();

        BigDecimal exchangeRateValue3 = exchangeRateApi
                .findByCurrencyAndDate(CURRENCY, DATE)
                .orElseThrow();

        Assertions.assertEquals(exchangeRateValue, exchangeRateValue1);

        Assertions.assertEquals(exchangeRateValue1, exchangeRateValue2);
        Assertions.assertEquals(exchangeRateValue1, exchangeRateValue3);

        Mockito.verify(exchangeRateApi, Mockito.times(1))
                .findByCurrencyAndStartDateAndEndDate(
                        Mockito.eq("BRL"), Mockito.any(), Mockito.eq(DATE)
                );
    }
}