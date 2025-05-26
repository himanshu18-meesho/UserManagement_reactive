package com.himanshu.userManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

import org.springframework.beans.factory.annotation.Value;

import com.himanshu.userManagement.model.User;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(
        @Value("${spring.data.redis.host}") String host,
        @Value("${spring.data.redis.port}") int port) {
        
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config, clientConfig);
    }
    
    @Bean
    @Primary
    public ReactiveRedisTemplate<String, User> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(User.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, User> builder = RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, User> context = builder.value(serializer).build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}