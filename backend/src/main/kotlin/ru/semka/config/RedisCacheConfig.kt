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

@Configuration
@EnableCaching
@org.springframework.context.annotation.Profile("!test")
class RedisCacheConfig {

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        val redisMapper = ObjectMapper().registerModule(kotlinModule())
        val serializer = Jackson2JsonRedisSerializer(redisMapper, BalanceDto::class.java)
        val cfg = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer),
            )
        return RedisCacheManager.builder(factory).cacheDefaults(cfg).build()
    }
}
