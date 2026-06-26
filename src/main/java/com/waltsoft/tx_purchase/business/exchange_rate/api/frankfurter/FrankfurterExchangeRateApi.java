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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class FrankfurterExchangeRateApi implements ExchangeRateApi {

    public static final int MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS = 6; // Similar to UsaTreasury
    private static final String API_BASE_URL = "https://api.frankfurter.app";
    private static final String API_PATH_TEMPLATE = "/%s..%s?from=%s&to=%s"; // /YYYY-MM-DD..YYYY-MM-DD?from=BASE&to=TARGET
    private static final String BASE_CURRENCY = "USD";
    private static final Log LOG = LogFactory.getLog(FrankfurterExchangeRateApi.class);

    private final WebClient webClient;
    private final Duration apiRequestTimeout;
    private final int apiRequestMaxAttempts;
    private final Duration apiRequestBackoffDuration;
    private final FrankfurterCurrencyCodeConverter currencyCodeConverter;

    @Autowired
    public FrankfurterExchangeRateApi(WebClient.Builder webClientBuilder, FrankfurterCurrencyCodeConverter currencyCodeConverter) {
        this.webClient = webClientBuilder.baseUrl(API_BASE_URL).build();
        this.apiRequestTimeout = Duration.ofSeconds(8);
        this.apiRequestMaxAttempts = 3;
        this.apiRequestBackoffDuration = Duration.ofSeconds(2);
        this.currencyCodeConverter = currencyCodeConverter;
    }

    public FrankfurterExchangeRateApi(WebClient webClient, Duration apiTimeout, int apiRequestMaxAttempts, Duration apiRequestBackoffDuration, FrankfurterCurrencyCodeConverter currencyCodeConverter) {
        this.webClient = webClient;
        this.apiRequestTimeout = apiTimeout;
        this.apiRequestMaxAttempts = apiRequestMaxAttempts;
        this.apiRequestBackoffDuration = apiRequestBackoffDuration;
        this.currencyCodeConverter = currencyCodeConverter;
    }

    @Override
    public Integer getPriority() {
        return 2;
    }

    @Override
    @Cacheable(
            value = FrankfurterExchangeRateCacheConfig.EXCHANGE_RATE_CACHE_NAME,
            key = "{#currency, #date}",
            cacheManager = "registerFrankfurterExchangeRateCacheManager"
    )
    @CircuitBreaker(
            name = FrankfurterExchangeRateCircuitBreakConfig.FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME,
            fallbackMethod = "findByCurrencyAndDateFallback"
    )
    public Optional<BigDecimal> findByCurrencyAndDate(String currency, LocalDate date) {
        Optional<String> convertedCurrencyCodeOptional = currencyCodeConverter.convert(currency);

        if (convertedCurrencyCodeOptional.isEmpty()) {
            LOG.warn(String.format("Could not convert currency '%s' to Frankfurter format.", currency));
            return Optional.empty();
        }

        String convertedCurrencyCode = convertedCurrencyCodeOptional.get();

        LocalDate startDate = date.minusMonths(MAX_EXCHANGE_RATES_PERIOD_IN_MONTHS);

        List<FrankfurterExchangeRateDto> exchangeRates = findByCurrencyAndStartDateAndEndDate(convertedCurrencyCode, startDate, date);

        if (exchangeRates.isEmpty()) {
            return Optional.empty();
        }

        FrankfurterExchangeRateDto exchangeRate = exchangeRates.stream()
                .max(Comparator.comparing(FrankfurterExchangeRateDto::date))
                .orElseThrow();

        return Optional.of(exchangeRate.value());
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

    List<FrankfurterExchangeRateDto> findByCurrencyAndStartDateAndEndDate(String targetCurrencyCode, LocalDate startDate, LocalDate endDate) {
        String path = String.format(API_PATH_TEMPLATE, startDate.toString(), endDate.toString(), BASE_CURRENCY, targetCurrencyCode);

        FrankfurterExchangeRateApiResponseDto apiResponse = this.webClient.get()
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

        if (apiResponse==null || apiResponse.rates()==null || apiResponse.rates().isEmpty()) {
            return List.of();
        }

        return apiResponse.rates().entrySet().stream()
                .flatMap(dateEntry -> {
                    LocalDate rateDate = dateEntry.getKey();
                    Map<String, BigDecimal> ratesForDate = dateEntry.getValue();
                    BigDecimal rateValue = ratesForDate.get(targetCurrencyCode);
                    if (rateValue!=null) {
                        return Stream.of(new FrankfurterExchangeRateDto(rateDate, rateValue));
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());
    }

    private UnavailableExchangeRateApiRuntimeException makeUnavailableExchangeRateApiRuntimeException() {
        return new UnavailableExchangeRateApiRuntimeException("Exchange rate service (Frankfurter) is currently unavailable. Please try again later.");
    }
}