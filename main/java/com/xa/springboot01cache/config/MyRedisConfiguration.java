package com.xa.springboot01cache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.LinkedHashSet;
import java.util.List;

public class MyRedisConfiguration {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        /*
        在SpringBoot的配置文件中是通过this.determineConfiguration(resourceLoader.getClassLoader())
        来获得org.springframework.data.redis.cache.RedisCacheConfiguration
        这个RedisCacheConfiguration是SpringBoot帮我们配置好的,我们如果自己创建一个默认的RedisCacheConfiguration
        则直接通过org.springframework.data.redis.cache.RedisCacheConfiguration中的defaultCacheConfig方法来获得默认的
        注意这里的RedisCacheConfiguration,
        一个是org.springframework.data.redis.cache.RedisCacheConfiguration
        另一个是org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
         */
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
        //RedisCacheManager 生成器创建
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(redisCacheConfiguration);
        return builder.build();
    }

}
