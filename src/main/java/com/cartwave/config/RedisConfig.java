package com.cartwave.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    public static final String CACHE_STORE_PUBLIC       = "store-public";
    public static final String CACHE_STORE_PRODUCTS     = "store-products";
    public static final String CACHE_SUBSCRIPTION_PLANS = "subscription-plans";
    public static final String CACHE_DASHBOARD_METRICS  = "dashboard-metrics";

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.activateDefaultTyping(
                    mapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL
            );

            GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                    .disableCachingNullValues();

            Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
            cacheConfigs.put(CACHE_STORE_PUBLIC,       defaultConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigs.put(CACHE_STORE_PRODUCTS,     defaultConfig.entryTtl(Duration.ofMinutes(3)));
            cacheConfigs.put(CACHE_SUBSCRIPTION_PLANS, defaultConfig.entryTtl(Duration.ofMinutes(60)));
            cacheConfigs.put(CACHE_DASHBOARD_METRICS,  defaultConfig.entryTtl(Duration.ofMinutes(2)));

            connectionFactory.getConnection().ping(); // test connection

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(5)))
                    .withInitialCacheConfigurations(cacheConfigs)
                    .build();
        } catch (Exception e) {
            log.warn("Redis unavailable â€” falling back to in-memory cache. Reason: {}", e.getMessage());
            return new ConcurrentMapCacheManager(); // dynamic - creates caches on demand for any name
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}


