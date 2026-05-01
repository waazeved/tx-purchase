package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.ExchangeRateApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

class FindExchangeRatesByCurrencyAndStartDateAndEndDateTest {

    public static final String CURRENCY = "Brazil-Real";
    public static final LocalDate START_DATE = LocalDate.now();
    public static final LocalDate END_DATE = LocalDate.now();
    private MockWebServer mockWebServer;
    private ExchangeRateServiceImpl exchangeRateService;

    @BeforeEach
    void initialize() throws IOException {

        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        WebClient.Builder webClientBuilder = WebClient.builder();
        WebClient webClient = webClientBuilder.baseUrl(mockWebServer.url("/").toString()).build();

        Duration apiRequestTimeout = Duration.ofSeconds(5);
        int apiRequestMaxAttempts = 3;
        Duration apiRequestBackoffDuration = Duration.ofMillis(10);

        this.exchangeRateService = new ExchangeRateServiceImpl(webClient, apiRequestTimeout, apiRequestMaxAttempts, apiRequestBackoffDuration);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return list of rates with success")
    void shouldReturnExchangeRatesWithSuccess() {
        String jsonResponse = """
                {
                    "data": [
                        {"record_date": "2023-12-31", "exchange_rate": "4.85"},
                        {"record_date": "2023-09-30", "exchange_rate": "5.01"}
                    ]
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<ExchangeRate> result = exchangeRateService.findExchangeRatesByCurrencyAndStartDateAndEndDate(
                CURRENCY,
                START_DATE, END_DATE);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("4.85", result.get(0).rate());
        Assertions.assertEquals(LocalDate.of(2023, 12, 31), result.get(0).date());
    }

    @Test
    @DisplayName("Should trigger Retry and succeed on the second attempt")
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

        List<ExchangeRate> result = exchangeRateService.findExchangeRatesByCurrencyAndStartDateAndEndDate(
                CURRENCY, START_DATE, END_DATE);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2, mockWebServer.getRequestCount());
    }

    @Test
    @DisplayName("Should return empty list if API finds no data")
    void shouldReturnEmptyListWhenNoDataFound() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"data": []}
                        """)
                .addHeader("Content-Type", "application/json"));

        List<ExchangeRate> result = exchangeRateService.findExchangeRatesByCurrencyAndStartDateAndEndDate(
                CURRENCY, START_DATE, END_DATE);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw ExchangeRateApiException after exhausting all Retries")
    void shouldThrowExceptionAfterAllRetriesFail() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Assertions.assertThrows(ExchangeRateApiException.class,
                () -> exchangeRateService.findExchangeRatesByCurrencyAndStartDateAndEndDate(
                        CURRENCY, START_DATE, END_DATE));

        Assertions.assertEquals(4, mockWebServer.getRequestCount());
    }
}