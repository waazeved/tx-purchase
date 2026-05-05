package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;
import com.waltsoft.tx_purchase.dto.exchange_rate.UsaTreasuryExchangeRateDto;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
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

class FindExchangeRateValueByCurrencyAndDateCacheTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();

    @MockitoSpyBean
    private ExchangeRateServiceImpl exchangeRateService;

    @Autowired
    @Qualifier("registerExchangeRateCacheManager")
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(ExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME);

        if (cache!=null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("Should use cache and not execute ExchangeRateServiceImpl.findExchangeRateValueByCurrencyAndDate")
    void shouldCacheExchangeRateResult() {
        String exchangeRateValeAsStr = "5.50";

        ExchangeRate mockRate = new UsaTreasuryExchangeRateDto(DATE, exchangeRateValeAsStr);
        List<ExchangeRate> exchangeRates = List.of(mockRate);

        Mockito.doReturn(exchangeRates).when(exchangeRateService)
                .findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate(
                        Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE)
                );

        BigDecimal exchangeRateValue1 = exchangeRateService
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE)
                .orElseThrow();

        BigDecimal exchangeRateValue2 = exchangeRateService
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE)
                .orElseThrow();

        BigDecimal exchangeRateValue3 = exchangeRateService
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE)
                .orElseThrow();

        BigDecimal exchangeRateValue = new BigDecimal(exchangeRateValeAsStr);
        Assertions.assertEquals(exchangeRateValue, exchangeRateValue1);

        Assertions.assertEquals(exchangeRateValue1, exchangeRateValue2);
        Assertions.assertEquals(exchangeRateValue1, exchangeRateValue3);

        Mockito.verify(exchangeRateService, Mockito.times(1))
                .findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate(
                        Mockito.eq(CURRENCY), Mockito.any(), Mockito.eq(DATE)
                );
    }
}