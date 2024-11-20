package org.agency.course_work.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Value("${cache.expire-after-write}")
    private Duration expireAfterWrite;

    @Value("${cache.maximum-size}")
    private int maximumSize;

    @Bean
    public CacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("agents", "players", "clubs","contracts","matches");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite)
                .maximumSize(maximumSize));
        return cacheManager;
    }

//    @Bean
//    public SimpleKeyGenerator keyGenerator() {
//        return new SimpleKeyGenerator();
//    }
}
