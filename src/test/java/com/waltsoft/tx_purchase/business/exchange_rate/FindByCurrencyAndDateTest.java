package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.api.ExchangeRateApi;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.ExchangeRateApiRuntimeException;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@DisplayName("ExchangeRateServiceImpl.findByCurrencyAndDate Tests")
@ExtendWith(MockitoExtension.class)
class FindByCurrencyAndDateTest {

    private static final String CURRENCY = "USD";
    private static final LocalDate DATE = LocalDate.of(2023, 1, 15);
    private static final BigDecimal EXCHANGE_RATE_VALUE_API_PRIORITY_1 = new BigDecimal("5.00");
    private static final BigDecimal EXCHANGE_RATE_VALUE_API_PRIORITY_2 = new BigDecimal("5.10");
    private static final BigDecimal EXCHANGE_RATE_VALUE_API_PRIORITY_3 = new BigDecimal("5.20");
    private static final Integer PRIORITY_1 = 1;
    private static final Integer PRIORITY_2 = 2;
    private static final Integer PRIORITY_3 = 3;
    private static final String UNAVAILABLE_API_MESSAGE = "API is unavailable";
    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected API error";
    private static final long CONCURRENCY_TEST_DELAY_SECONDS = 2;

    private ExchangeRateServiceImpl exchangeRateService;

    @Mock
    private ExchangeRateApi mockApiPriority1;
    @Mock
    private ExchangeRateApi mockApiPriority2;
    @Mock
    private ExchangeRateApi mockApiPriority3;

    @Test
    @DisplayName("Should return value from highest priority API when multiple APIs return values")
    void shouldReturnValueFromHighestPriorityApiWhenMultipleApisReturnValues() {

        mockPriorities(mockApiPriority1, mockApiPriority2, mockApiPriority3);

        mockReturnValue(mockApiPriority1, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_1));
        mockReturnValue(mockApiPriority2, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_2));
        mockReturnValue(mockApiPriority3, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_3));

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2, mockApiPriority3);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(EXCHANGE_RATE_VALUE_API_PRIORITY_1, result.get());
    }

    @Test
    @DisplayName("Should return value from next highest priority API when highest priority returns empty")
    void shouldReturnValueFromNextHighestPriorityApiWhenHighestPriorityReturnsEmpty() {

        mockPriorities(mockApiPriority1, mockApiPriority2, mockApiPriority3);

        mockReturnValue(mockApiPriority1, Optional.empty());
        mockReturnValue(mockApiPriority2, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_2));
        mockReturnValue(mockApiPriority3, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_3));

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2, mockApiPriority3);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(EXCHANGE_RATE_VALUE_API_PRIORITY_2, result.get());
    }

    @Test
    @DisplayName("Should return empty when all APIs return empty")
    void shouldReturnEmptyWhenAllApisReturnEmpty() {

        mockPriorities(mockApiPriority1, mockApiPriority2);

        mockReturnValue(mockApiPriority1, Optional.empty());
        mockReturnValue(mockApiPriority2, Optional.empty());

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return value when only one API is configured and returns value")
    void shouldReturnValueWhenOnlyOneApiIsConfiguredAndReturnsValue() {

        mockPriorities(mockApiPriority1);

        mockReturnValue(mockApiPriority1, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_1));

        List<ExchangeRateApi> apis = List.of(mockApiPriority1);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(EXCHANGE_RATE_VALUE_API_PRIORITY_1, result.get());
    }

    @Test
    @DisplayName("Should return empty when only one API is configured and returns empty")
    void shouldReturnEmptyWhenOnlyOneApiIsConfiguredAndReturnsEmpty() {

        mockPriorities(mockApiPriority1);

        mockReturnValue(mockApiPriority1, Optional.empty());

        List<ExchangeRateApi> apis = List.of(mockApiPriority1);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw UnavailableExchangeRateApiRuntimeException when all APIs throw UnavailableExchangeRateApiRuntimeException")
    void shouldThrowUnavailableExceptionWhenAllApisThrowUnavailable() {

        mockPriorities(mockApiPriority1, mockApiPriority2);

        mockThrowUnavailable(mockApiPriority1);
        mockThrowUnavailable(mockApiPriority2);

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class,
                () -> exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE));
    }

    @Test
    @DisplayName("Should return empty when some APIs throw Unavailable and others return empty")
    void shouldReturnEmptyWhenSomeApisThrowUnavailableAndOthersReturnEmpty() {

        mockPriorities(mockApiPriority1, mockApiPriority2, mockApiPriority3);

        mockThrowUnavailable(mockApiPriority1);
        mockReturnValue(mockApiPriority2, Optional.empty());
        mockThrowUnavailable(mockApiPriority3);

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2, mockApiPriority3);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should propagate unexpected exception")
    void shouldPropagateUnexpectedException() {

        mockPriorities(mockApiPriority1, mockApiPriority2);

        mockThrowRuntimeException(mockApiPriority1);
        mockReturnValue(mockApiPriority2, Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_2));

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Assertions.assertThrows(ExchangeRateApiRuntimeException.class,
                () -> exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE));
    }

    @Test
    @DisplayName("Should throw UnavailableExchangeRateApiRuntimeException when no APIs are configured")
    void shouldThrowUnavailableExceptionWhenNoApisAreConfigured() {
        List<ExchangeRateApi> apis = List.of();
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class,
                () -> exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE));
    }

    @SuppressWarnings("java:S2925")
    @ParameterizedTest
    @MethodSource("shouldHandleApiCallsThatTakeLongerThanOthersWithoutBlockingMainThread")
    @DisplayName("Should handle API calls that take longer than others without blocking the main thread")
    void shouldHandleApiCallsThatTakeLongerThanOthersWithoutBlockingMainThread(
            Optional<BigDecimal> exchangeRateValueApiPriority1,
            Optional<BigDecimal> exchangeRateValueApiPriority2,
            BigDecimal expectedExchangeRateValue) {

        mockPriorities(mockApiPriority1, mockApiPriority2);

        mockReturnValue(mockApiPriority1, exchangeRateValueApiPriority1);

        Mockito.doAnswer(invocation -> {
            TimeUnit.SECONDS.sleep(CONCURRENCY_TEST_DELAY_SECONDS);
            return exchangeRateValueApiPriority2;
        }).when(mockApiPriority2).findByCurrencyAndDate(ArgumentMatchers.any(), ArgumentMatchers.any());

        List<ExchangeRateApi> apis = List.of(mockApiPriority1, mockApiPriority2);
        exchangeRateService = new ExchangeRateServiceImpl(apis);

        Optional<BigDecimal> result = exchangeRateService.findByCurrencyAndDate(CURRENCY, DATE);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expectedExchangeRateValue, result.get());
    }

    private static Stream<Arguments> shouldHandleApiCallsThatTakeLongerThanOthersWithoutBlockingMainThread() {
        return Stream.of(
                Arguments.of(
                        Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_1),
                        Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_2),
                        EXCHANGE_RATE_VALUE_API_PRIORITY_1),
                Arguments.of(
                        Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_1),
                        Optional.empty(),
                        EXCHANGE_RATE_VALUE_API_PRIORITY_1),
                Arguments.of(
                        Optional.empty(),
                        Optional.of(EXCHANGE_RATE_VALUE_API_PRIORITY_2),
                        EXCHANGE_RATE_VALUE_API_PRIORITY_2)
        );
    }

    private void mockPriorities(ExchangeRateApi... apis) {

        Map<ExchangeRateApi, Integer> mockPriorities = new HashMap<>();
        mockPriorities.put(mockApiPriority1, PRIORITY_1);
        mockPriorities.put(mockApiPriority2, PRIORITY_2);
        mockPriorities.put(mockApiPriority3, PRIORITY_3);

        for (ExchangeRateApi api : apis) {
            Integer priority = mockPriorities.get(api);

            if (priority!=null) {
                Mockito.when(api.getPriority()).thenReturn(priority);
            }

        }
    }

    private void mockReturnValue(ExchangeRateApi api, Optional<BigDecimal> value) {
        Mockito.when(api.findByCurrencyAndDate(CURRENCY, DATE))
                .thenReturn(value);
    }

    private void mockThrowUnavailable(ExchangeRateApi api) {
        Mockito.when(api.findByCurrencyAndDate(CURRENCY, DATE))
                .thenThrow(new UnavailableExchangeRateApiRuntimeException(UNAVAILABLE_API_MESSAGE));
    }

    private void mockThrowRuntimeException(ExchangeRateApi api) {
        Mockito.when(api.findByCurrencyAndDate(CURRENCY, DATE))
                .thenThrow(new RuntimeException(UNEXPECTED_ERROR_MESSAGE));
    }
}


