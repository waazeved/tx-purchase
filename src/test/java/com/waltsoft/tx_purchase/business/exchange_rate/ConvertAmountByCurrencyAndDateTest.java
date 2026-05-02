package com.waltsoft.tx_purchase.business.exchange_rate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

public class ConvertAmountByCurrencyAndDateTest {


    public static final String CURRENCY = "Brazil-Real";

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
    @DisplayName("Should convert amount by currency and date")
    void shouldConvertAmountByCurrencyAndDate() {
        LocalDate targetDate = LocalDate.now();
        BigDecimal exchangeRateValue = new BigDecimal("2.05");
        BigDecimal amount = new BigDecimal("100");
        BigDecimal expectedConvertedAmount = new BigDecimal("205.00");

        Mockito.doReturn(exchangeRateValue)
                .when(exchangeRateService)
                .findExchangeRateValueByCurrencyAndDate(
                        CURRENCY,
                        targetDate
                );

        BigDecimal convertedAmount = this.exchangeRateService
                .convertAmountByCurrencyAndDate(amount, CURRENCY, targetDate);

        Assertions.assertNotNull(convertedAmount);
        Assertions.assertEquals(expectedConvertedAmount, convertedAmount);
    }
}
