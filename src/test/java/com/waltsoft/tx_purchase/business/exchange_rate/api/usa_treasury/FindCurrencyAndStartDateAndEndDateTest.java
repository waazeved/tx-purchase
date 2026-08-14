package com.waltsoft.tx_purchase.business.exchange_rate.api.usa_treasury;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@DisplayName("UsaTreasuryExchangeRateApi.findByCurrencyAndStartDateAndEndDate Tests")
class FindCurrencyAndStartDateAndEndDateTest {

    public static final String CURRENCY = "Brazil-Real";
    public static final LocalDate START_DATE = LocalDate.of(2025, 12, 30);
    public static final LocalDate END_DATE = LocalDate.of(2025, 12, 31);
    private MockWebServer mockWebServer;
    private UsaTreasuryExchangeRateApi exchangeRateApi;

    @BeforeEach
    void initialize() throws IOException {

        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        WebClient.Builder webClientBuilder = WebClient.builder();
        WebClient webClient = webClientBuilder.baseUrl(mockWebServer.url("/").toString()).build();

        Duration apiRequestTimeout = Duration.ofSeconds(5);
        int apiRequestMaxAttempts = 3;
        Duration apiRequestBackoffDuration = Duration.ofMillis(10);

        this.exchangeRateApi = new UsaTreasuryExchangeRateApi(webClient, apiRequestTimeout, apiRequestMaxAttempts, apiRequestBackoffDuration);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should find exchange rates from API and return list of rates with success")
    void shouldReturnExchangeRatesWithSuccess() {

        String rate1 = "5.01";
        String rate2 = "4.85";

        String jsonResponse = """
                 {
                     "data": [
                         {"record_date": "%s", "exchange_rate": "%s" },
                         {"record_date": "%s", "exchange_rate": "%s"}
                     ]
                 }
                """.formatted(START_DATE, rate1, END_DATE, rate2);

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<UsaTreasuryExchangeRateDto> exchangeRates = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                CURRENCY,
                START_DATE, END_DATE);

        Assertions.assertNotNull(exchangeRates);
        Assertions.assertEquals(2, exchangeRates.size());

        UsaTreasuryExchangeRateDto exchangeRate1 = exchangeRates.get(0);
        UsaTreasuryExchangeRateDto exchangeRate2 = exchangeRates.get(1);

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
                          "data": [
                            {"record_date": "2023-12-31", "exchange_rate": "4.85"}
                          ]
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        List<UsaTreasuryExchangeRateDto> result = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
                CURRENCY, START_DATE, END_DATE);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2, mockWebServer.getRequestCount());
    }

    @Test
    @DisplayName("Should find exchange rates from API and return empty list if finds no data")
    void shouldReturnEmptyListWhenNoDataFound() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"data": []}
                        """)
                .addHeader("Content-Type", "application/json"));

        List<UsaTreasuryExchangeRateDto> result = exchangeRateApi.findByCurrencyAndStartDateAndEndDate(
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
}