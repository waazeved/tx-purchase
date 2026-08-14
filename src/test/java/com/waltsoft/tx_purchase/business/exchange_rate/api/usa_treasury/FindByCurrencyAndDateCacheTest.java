package com.waltsoft.tx_purchase.business.exchange_rate.api.usa_treasury;

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

@DisplayName("UsaTreasuryExchangeRateApi.findByCurrencyAndDate cache tests")
class FindByCurrencyAndDateCacheTest extends ContainerTest {

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

        Cache cache = cacheManager.getCache(UsaTreasuryExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME);

        if (cache!=null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("Should use cache and not execute UsaTreasuryExchangeRateApi.findExchangeRateValueByCurrencyAndDate")
    void shouldCacheExchangeRateResult() {
        String exchangeRateValeAsStr = "5.50";

        UsaTreasuryExchangeRateDto mockRate = new UsaTreasuryExchangeRateDto(DATE, exchangeRateValeAsStr);
        List<UsaTreasuryExchangeRateDto> exchangeRates = List.of(mockRate);

        Mockito.doReturn(exchangeRates).when(exchangeRateApi)
                .findByCurrencyAndStartDateAndEndDate(
                        Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE)
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

        BigDecimal exchangeRateValue = new BigDecimal(exchangeRateValeAsStr);
        Assertions.assertEquals(exchangeRateValue, exchangeRateValue1);

        Assertions.assertEquals(exchangeRateValue1, exchangeRateValue2);
        Assertions.assertEquals(exchangeRateValue1, exchangeRateValue3);

        Mockito.verify(exchangeRateApi, Mockito.times(1))
                .findByCurrencyAndStartDateAndEndDate(
                        Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE)
                );
    }
}