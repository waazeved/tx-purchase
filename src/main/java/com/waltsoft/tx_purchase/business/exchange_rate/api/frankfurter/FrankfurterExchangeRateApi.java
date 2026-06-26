package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import com.waltsoft.tx_purchase.business.exchange_rate.api.ExchangeRateApi;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Component
class FrankfurterExchangeRateApi implements ExchangeRateApi {

    private static final String API_BASE_URL = "https://api.frankfurter.app";
    private static final String API_PATH_TEMPLATE = "/%s?from=%s&to=%s"; // /YYYY-MM-DD?from=BASE&to=TARGET
    private static final String BASE_CURRENCY = "USD"; // Assuming USD as base for simplicity, similar to USA Treasury API context
    private static final Log LOG = LogFactory.getLog(FrankfurterExchangeRateApi.class);

    private final WebClient webClient;
    private final Duration apiRequestTimeout;
    private final int apiRequestMaxAttempts;
    private final Duration apiRequestBackoffDuration;
    private final FrankfurterCurrencyCodeMapper currencyCodeMapper;

    @Autowired
    public FrankfurterExchangeRateApi(WebClient.Builder webClientBuilder, FrankfurterCurrencyCodeMapper currencyCodeMapper) {
        this.webClient = webClientBuilder.baseUrl(API_BASE_URL).build();
        this.apiRequestTimeout = Duration.ofSeconds(8);
        this.apiRequestMaxAttempts = 3;
        this.apiRequestBackoffDuration = Duration.ofSeconds(2);
        this.currencyCodeMapper = currencyCodeMapper;
    }

    // Constructor for testing purposes
    public FrankfurterExchangeRateApi(WebClient webClient, Duration apiTimeout, int apiRequestMaxAttempts, Duration apiRequestBackoffDuration, FrankfurterCurrencyCodeMapper currencyCodeMapper) {
        this.webClient = webClient;
        this.apiRequestTimeout = apiTimeout;
        this.apiRequestMaxAttempts = apiRequestMaxAttempts;
        this.apiRequestBackoffDuration = apiRequestBackoffDuration;
        this.currencyCodeMapper = currencyCodeMapper;
    }

    @Override
    @Cacheable(value = FrankfurterExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME, key = "{#currency, #date}")
    @CircuitBreaker(name = FrankfurterExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME, fallbackMethod = "findByCurrencyAndDateFallback")
    public Optional<BigDecimal> findByCurrencyAndDate(String currency, LocalDate date) {
        String targetCurrencyCode = currencyCodeMapper.parseCurrencyToFrankfurterFormat(currency);

        if (targetCurrencyCode==null) {
            LOG.warn(String.format("Could not parse currency '%s' to Frankfurter format.", currency));
            return Optional.empty();
        }

        if (BASE_CURRENCY.equals(targetCurrencyCode)) {
            return Optional.of(BigDecimal.ONE); // Exchange rate of USD to USD is 1
        }

        String path = String.format(API_PATH_TEMPLATE, date.toString(), BASE_CURRENCY, targetCurrencyCode);

        FrankfurterExchangeRateApiResponseDto response = this.webClient.get()
                .uri(path)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        resp -> Mono.error(new UnavailableExchangeRateApiRuntimeException("Frankfurter API Error: " + resp.statusCode())))
                .bodyToMono(FrankfurterExchangeRateApiResponseDto.class)
                .timeout(this.apiRequestTimeout)
                .retryWhen(Retry.backoff(this.apiRequestMaxAttempts, this.apiRequestBackoffDuration)
                        .filter(RuntimeException.class::isInstance)
                        .doBeforeRetry(retrySignal ->
                                LOG.warn("Retrying request to Frankfurter exchange rate API... Attempt: " + (retrySignal.totalRetries() + 1)))
                )
                .onErrorResume(e -> {
                    LOG.error("All retries to Frankfurter exchange rate API failed. Error: " + e.getMessage());
                    return Mono.error(
                            makeUnavailableExchangeRateApiRuntimeException()
                    );
                }).block();

        return Optional.ofNullable(response)
                .map(r -> r.rates().get(targetCurrencyCode));
    }

    @SuppressWarnings({"java:S1172", "java:S112"})
    public Optional<BigDecimal> findByCurrencyAndDateFallback(
            String currency, LocalDate date, Exception exception) throws Exception {

        if (exception instanceof CallNotPermittedException) {
            throw makeUnavailableExchangeRateApiRuntimeException();
        }

        LOG.error(String.format("Error on FrankfurterExchangeRateApi.findByCurrencyAndDate. Currency: %s, Date: %s. Error: %s",
                currency, date, exception.getMessage()));

        throw exception;
    }

    private UnavailableExchangeRateApiRuntimeException makeUnavailableExchangeRateApiRuntimeException() {
        return new UnavailableExchangeRateApiRuntimeException("Exchange rate service (Frankfurter) is currently unavailable. Please try again later.");
    }

    @Override
    public Integer getPriority() {
        return 2;
    }
}
