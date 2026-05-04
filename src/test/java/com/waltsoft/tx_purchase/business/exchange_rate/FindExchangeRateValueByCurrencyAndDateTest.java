package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataRuntimeException;
import com.waltsoft.tx_purchase.dto.exchange_rate.UsaTreasuryExchangeRateDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class FindExchangeRateValueByCurrencyAndDateTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();

    private ExchangeRateServiceImpl exchangeRateService;

    @BeforeEach
    void setUp() {
        WebClient webClient = Mockito.mock(WebClient.class);
        exchangeRateService = Mockito.spy(new ExchangeRateServiceImpl(
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
        LocalDate startDate = targetDate.minusMonths(ExchangeRateServiceImpl.MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS);

        LocalDate date1 = targetDate.minusMonths(1);
        LocalDate date2 = date1.minusDays(1);
        LocalDate date3 = date2.minusDays(1);

        String rate1 = "4.50";
        String rate2 = "5.00";
        String rate3 = "3.25";

        ExchangeRate exchangeRate1 = new UsaTreasuryExchangeRateDto(date1, rate1);
        ExchangeRate exchangeRate2 = new UsaTreasuryExchangeRateDto(date2, rate2);
        ExchangeRate exchangeRate3 = new UsaTreasuryExchangeRateDto(date3, rate3);

        List<ExchangeRate> exchangeRates = List.of(exchangeRate1, exchangeRate2, exchangeRate3);

        Mockito.doReturn(exchangeRates)
                .when(exchangeRateService)
                .findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate(
                        CURRENCY,
                        startDate,
                        targetDate
                );

        BigDecimal expectedExchangeRateValue = new BigDecimal(rate1);
        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateService.findExchangeRateValueByCurrencyAndDate(CURRENCY, targetDate);

        Assertions.assertTrue(exchangeRateValueOptional.isPresent());
        Assertions.assertEquals(expectedExchangeRateValue, exchangeRateValueOptional.get());
    }

    @Test
    @DisplayName("Should return empty Optional when there is no exchange rate for date")
    void shouldThrowExceptionWhenNoRatesFound() {

        Mockito.doReturn(Collections.emptyList())
                .when(exchangeRateService)
                .findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate(
                        ArgumentMatchers.eq(CURRENCY),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(DATE)
                );

        Optional<BigDecimal> exchangeRateValueOptional = exchangeRateService
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(exchangeRateValueOptional.isEmpty());
    }
}