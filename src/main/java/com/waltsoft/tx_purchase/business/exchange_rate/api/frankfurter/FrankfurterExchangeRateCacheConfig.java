package com.waltsoft.tx_purchase.business.exchange_rate.api.frankfurter;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
class FrankfurterExchangeRateCacheConfig {
    public static final String EXCHANGE_RATE_CACHE_NAME = "frankfurterExchangeRateCache";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(EXCHANGE_RATE_CACHE_NAME);
    }
}
