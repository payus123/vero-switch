package com.vero.coreprocessor.config;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableCaching
public class RedisConfiguration {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;


    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(){
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);



        if (redisPassword != null) configuration.setPassword(redisPassword);

        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().build();

        JedisConnectionFactory factory =  new JedisConnectionFactory(configuration, jedisClientConfiguration);

        factory.afterPropertiesSet();
        redisTemplate.setConnectionFactory(factory);

        return redisTemplate;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }



    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serialization for both keys and values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }


}
