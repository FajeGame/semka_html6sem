package ru.semka.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import ru.semka.dto.BalanceDto
import java.time.Duration

/**
 * кэш Spring Cache в Redis: сейчас используется только для balance (WalletService.getBalance).
 * ObjectMapper создаётся локально — НЕ @Bean, иначе Jackson ломает обычный JSON REST-ответов.
 * TTL 5 минут; при изменении операций кэш сбрасывается через @CacheEvict.
 */
@Configuration
@EnableCaching
@org.springframework.context.annotation.Profile("!test")
class RedisCacheConfig {

    // менеджер кэша: одна запись balance:{walletId} → BalanceDto
    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        val redisMapper = ObjectMapper().registerModule(kotlinModule()) // только для Redis, не @Bean
        val serializer = Jackson2JsonRedisSerializer(redisMapper, BalanceDto::class.java)
        val cfg = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // 5 минут, сброс при операциях — @CacheEvict
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer),
            )
        return RedisCacheManager.builder(factory).cacheDefaults(cfg).build()
    }
}
