package com.waltsoft.tx_purchase.business.exchange_rate;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
class ExchangeRateCacheConfig {


    public static final String EXCHANGE_RATE_CACHE_NAME = "exchangeRate";
    public static final int EXCHANGE_RATES_CACHE_MAXIMUM_SIZE = 500;
    public static final int EXCHANGE_RATES_CACHE_DURATION = 12;

    @Bean
    public CacheManager registerExchangeRateCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(EXCHANGE_RATE_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(EXCHANGE_RATES_CACHE_MAXIMUM_SIZE)
                .expireAfterWrite(EXCHANGE_RATES_CACHE_DURATION, TimeUnit.HOURS)
                .recordStats());
        return cacheManager;
    }
}