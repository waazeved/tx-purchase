package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.NoExchangeRateDataRuntimeException;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import com.waltsoft.tx_purchase.test_container.ContainerTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Execution(ExecutionMode.SAME_THREAD)
class ConvertAmountByCurrencyAndDateTest extends ContainerTest {

    private static final String CURRENCY = "Brazil-Real";
    private static final LocalDate DATE = LocalDate.now();

    @MockitoSpyBean
    private ExchangeRateServiceImpl exchangeRateService;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry
                .circuitBreaker(ExchangeRateResilienceConfig.CONVERT_CIRCUIT_BREAKER)
                .reset();
    }

    @Test
    @DisplayName("Should convert amount by exchange rate data")
    void shouldConvertAmountByCurrencyAndDate() throws NoExchangeRateDataRuntimeException {
        BigDecimal exchangeRateValue = new BigDecimal("2.05");
        Optional<BigDecimal> exchangeRateValueOptional = Optional.of(exchangeRateValue);
        BigDecimal amount = new BigDecimal("100");
        BigDecimal expectedConvertedAmount = new BigDecimal("205.00");


        Mockito.doReturn(exchangeRateValueOptional)
                .when(exchangeRateService)
                .findExchangeRateValueByCurrencyAndDate(
                        CURRENCY,
                        DATE
                );

        BigDecimal convertedAmount = this.exchangeRateService
                .convertAmountByCurrencyAndDate(amount, CURRENCY, DATE);

        Assertions.assertNotNull(convertedAmount);
        Assertions.assertEquals(expectedConvertedAmount, convertedAmount);
    }

    @Test
    @DisplayName("Should throw NoExchangeRateDataException when there is no exchange rate data available for a specific currency and date")
    void shouldReturnExceptionWhenThereIsNoExchangeRateData() throws NoExchangeRateDataRuntimeException {
        BigDecimal amount = new BigDecimal("10.00");

        Mockito.doThrow(new NoExchangeRateDataRuntimeException("No data"))
                .when(exchangeRateService)
                .findExchangeRateValueByCurrencyAndDate(
                        CURRENCY,
                        DATE
                );

        Assertions.assertThrows(NoExchangeRateDataRuntimeException.class, () -> this.exchangeRateService
                .convertAmountByCurrencyAndDate(amount, CURRENCY, DATE));
    }

    @Test
    @DisplayName("Should circuit break when exchange rate API failure exceeds limits defined in ExchangeRateResilienceConfig.registerExchangeRateCircuitBreaker")
    void shouldOpenCircuitWhenFailureRateExceedsThreshold() throws NoExchangeRateDataRuntimeException {
        BigDecimal amount = new BigDecimal("10");
        Optional<BigDecimal> amountOptional = Optional.of(amount);

        Mockito.doThrow(new UnavailableExchangeRateApiRuntimeException("API Fail"))
                .doThrow(new UnavailableExchangeRateApiRuntimeException("API Fail"))
                .doReturn(amountOptional)
                .doReturn(amountOptional)
                .doThrow(new UnavailableExchangeRateApiRuntimeException("API Fail"))
                .when(exchangeRateService)
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE);

        for (int i = 1; i <= 5; i++) {
            try {
                this.exchangeRateService.convertAmountByCurrencyAndDate(amount, CURRENCY, DATE);
            } catch (UnavailableExchangeRateApiRuntimeException ignored) {
                //Ignore error
            }
        }

        Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class, () ->
                this.exchangeRateService.convertAmountByCurrencyAndDate(amount, CURRENCY, DATE));

        Mockito.verify(exchangeRateService, Mockito.times(5))
                .findExchangeRateValueByCurrencyAndDate(CURRENCY, DATE);
    }
}
