package com.waltsoft.tx_purchase.business.exchange_rate;

import com.waltsoft.tx_purchase.business.exchange_rate.exception.UnavailableExchangeRateApiRuntimeException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
class ExchangeRateResilienceConfig {

    public static final String CONVERT_CIRCUIT_BREAKER = "usaTreasuryExchangeRateApiCircuitBreaker";

    @Bean
    public String registerExchangeRateCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .recordExceptions(UnavailableExchangeRateApiRuntimeException.class)
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .permittedNumberOfCallsInHalfOpenState(2)
                .minimumNumberOfCalls(5)
                .build();

        registry.circuitBreaker(CONVERT_CIRCUIT_BREAKER, config);

        return CONVERT_CIRCUIT_BREAKER;
    }
}
