package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DisplayName("FrankfurterExchangeRateApi.findByCurrencyAndStartDateAndEndDate Tests")
class FindCurrencyAndStartDateAndEndDateTest {

    public static final String CURRENCY = "BRL";
    public static final LocalDate START_DATE = LocalDate.of(2025, 12, 30);
    public static final LocalDate END_DATE = LocalDate.of(2025, 12, 31);
    private MockWebServer mockWebServer;
    private FrankfurterExchangeRateApi exchangeRateApi;

    @BeforeEach
    void initialize() throws IOException {

        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        WebClient.Builder webClientBuilder = WebClient.builder();
        WebClient webClient = webClientBuilder.baseUrl(mockWebServer.url("/").toString()).build();

        Duration apiRequestTimeout = Duration.ofSeconds(5);
        int apiRequestMaxAttempts = 3;
        Duration apiRequestBackoffDuration = Duration.ofMillis(10);

        FrankfurterCurrencyCodeConverter currencyCodeConverter = Mockito.mock(FrankfurterCurrencyCodeConverter.class);
        Mockito.when(currencyCodeConverter.convert(CURRENCY)).thenReturn(Optional.of("BRL"));

        this.exchangeRateApi = new FrankfurterExchangeRateApi(webClient, apiRequestTimeout, apiRequestMaxAttempts, apiRequestBackoffDuration, currencyCodeConverter);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should find exchange rates from API and return list of rates with success")
    void shouldReturnExchangeRatesWithSuccess() {

        BigDecimal rate1 = new BigDecimal("5.01");
        BigDecimal rate2 = new BigDecimal("4.85");

        String jsonResponse = """
                 {
                     "rates": {
                         "%s": { "BRL": %s },
                         "%s": { "BRL": %s }
                     }
                 }
                """.formatted(START_DATE, rate1, END_DATE, rate2);

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<FrankfurterExchangeRateDto> exchangeRates = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                CURRENCY,
                START_DATE, END_DATE);

        Assertions.assertNotNull(exchangeRates);
        Assertions.assertEquals(2, exchangeRates.size());

        FrankfurterExchangeRateDto exchangeRate1 = exchangeRates.get(0);
        FrankfurterExchangeRateDto exchangeRate2 = exchangeRates.get(1);

        Assertions.assertEquals(rate1, exchangeRate1.value());
        Assertions.assertEquals(START_DATE, exchangeRate1.date());

        Assertions.assertEquals(rate2, exchangeRate2.value());
        Assertions.assertEquals(END_DATE, exchangeRate2.date());
    }

    @Test
    @DisplayName("Should find exchange rates from API, trigger Retry and succeed on the second attempt")
    void shouldRetryAndSucceedOnSecondAttempt() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "rates": {
                            "2023-12-31": { "BRL": 4.85 }
                          }
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        List<FrankfurterExchangeRateDto> result = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                CURRENCY, START_DATE, END_DATE);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2, mockWebServer.getRequestCount());
    }

    @Test
    @DisplayName("Should find exchange rates from API and return empty list if finds no data")
    void shouldReturnEmptyListWhenNoDataFound() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"rates": {}}
                        """)
                .addHeader("Content-Type", "application/json"));

        List<FrankfurterExchangeRateDto> result = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                CURRENCY, START_DATE, END_DATE);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find exchange rates from API and throw ExchangeRateApiException after exhausting all Retries")
    void shouldThrowExceptionAfterAllRetriesFail() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Assertions.assertThrows(UnavailableExchangeRateApiRuntimeException.class,
                () -> exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                        CURRENCY, START_DATE, END_DATE));

        Assertions.assertEquals(4, mockWebServer.getRequestCount());
    }

    @Test
    @DisplayName("Should return empty list when API returns 404 Not Found")
    void shouldReturnEmptyListWhenApiReturns404() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        List<FrankfurterExchangeRateDto> result = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                CURRENCY, START_DATE, END_DATE);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}