package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.data.ExchangeRate;
import com.waltsoft.tx_purchase.business.exchange_rate.exception.ExchangeRateApiException;
import com.waltsoft.tx_purchase.dto.exchange_rate.UsaTreasuryExchangeRateApiResponseDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
class ExchangeRateServiceImpl {

    private static final String API_BASE_URL = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1";
    private static final String API_PATH = "/accounting/od/rates_of_exchange";
    private static final String FILTER_PARAM = "filter";
    private static final String FIELDS_PARAM = "fields";
    private static final String FILTER_VALUE_TEMPLATE = "country_currency_desc:eq:%s,record_date:gte:%s,record_date:lte:%s";
    private static final String FIELDS_PARAM_VALUE = "record_date,exchange_rate";
    private static final Log LOG = LogFactory.getLog(ExchangeRateServiceImpl.class);

    private final WebClient webClient;
    private final Duration apiRequestTimeout;
    private final int apiRequestMaxAttempts;
    private final Duration apiRequestBackoffDuration;

    @Autowired
    public ExchangeRateServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(API_BASE_URL).build();
        this.apiRequestTimeout = Duration.ofSeconds(2);
        this.apiRequestMaxAttempts = 3;
        this.apiRequestBackoffDuration = Duration.ofSeconds(2);
    }

    public ExchangeRateServiceImpl(WebClient webClient, Duration apiTimeout, int apiRequestMaxAttempts, Duration apiRequestBackoffDuration) {
        this.webClient = webClient;
        this.apiRequestTimeout = apiTimeout;
        this.apiRequestMaxAttempts = apiRequestMaxAttempts;
        this.apiRequestBackoffDuration = apiRequestBackoffDuration;
    }

    List<ExchangeRate> findExchangeRatesFromApiByCurrencyAndStartDateAndEndDate(String currency, LocalDate startDate, LocalDate endDate) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_PATH)
                        .queryParam(FILTER_PARAM, String.format(FILTER_VALUE_TEMPLATE, currency, startDate, endDate))
                        .queryParam(FIELDS_PARAM, FIELDS_PARAM_VALUE)
                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ExchangeRateApiException("USA Treasury API Error")))
                .bodyToMono(UsaTreasuryExchangeRateApiResponseDto.class)
                .timeout(this.apiRequestTimeout)
                .map(response -> response.data().stream()
                        .map(ExchangeRate.class::cast)
                        .toList()
                )
                .retryWhen(Retry.backoff(this.apiRequestMaxAttempts, this.apiRequestBackoffDuration)
                        .filter(RuntimeException.class::isInstance)
                        .doBeforeRetry(retrySignal ->
                                LOG.warn("Retrying request to USA Treasury API... Attempt: " + (retrySignal.totalRetries() + 1)))
                )
                .onErrorResume(e -> Mono.error(
                        new ExchangeRateApiException("All requests to USA Treasury API failed. Error: " + e.getMessage())
                )).block();
    }

}
