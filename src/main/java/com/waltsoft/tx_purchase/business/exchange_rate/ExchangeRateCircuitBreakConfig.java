package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
class ExchangeRateCircuitBreakConfig {

    public static final String FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME = "findExchangeRateCircuitBreaker";
    public static final int FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_MIN_NUMBER_OF_CALLS = 10;
    public static final int FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD = 50;
    public static final int FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE = 5;

    @Bean
    public String registerFindExchangeRateCircuitBreaker(CircuitBreakerRegistry registry) {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .recordExceptions(UnavailableExchangeRateApiRuntimeException.class)
                .failureRateThreshold(FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD)
                .slidingWindowSize(FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .permittedNumberOfCallsInHalfOpenState(2)
                .minimumNumberOfCalls(FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_MIN_NUMBER_OF_CALLS)
                .build();

        registry.circuitBreaker(FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME, config);

        return FIND_EXCHANGE_RATE_CIRCUIT_BREAKER_NAME;
    }
}
