package com.pegallardo.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.pegallardo.gateway.middleware.ErrorHandlingMiddleware;
import com.pegallardo.gateway.middleware.RedisCachingMiddleware;
import com.pegallardo.gateway.middleware.RequestLoggingMiddleware;

@Configuration
public class AppConfig {

    // Configuration properties for Redis
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Autowired
    private RedisCachingMiddleware redisCachingMiddleware;

    @Autowired
    private RequestLoggingMiddleware requestLoggingMiddleware;

    @Autowired
    private ErrorHandlingMiddleware errorHandlingMiddleware;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return redisTemplate;
    }

    /**
     * Registers the error handling middleware as a filter that handles all "/api/*" endpoints.
     * This middleware catches all exceptions and errors, and returns a standardized API error response.
     * The order of this filter is set to the highest precedence, so it will catch any exceptions that occur
     * in other filters before they reach the application's endpoints.
     * @param errorHandlingMiddleware the error handling middleware to register
     * @return a filter registration bean for the error handling middleware
     */
    @Bean
    public FilterRegistrationBean<ErrorHandlingMiddleware> errorHandlingFilter() {
        var registrationBean = new FilterRegistrationBean<ErrorHandlingMiddleware>();
        registrationBean.setFilter(errorHandlingMiddleware);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    /**
     * Registers the request logging middleware as a filter that logs all "/api/*" endpoint requests and responses.
     * This middleware logs the request and response bodies, headers, and status codes, and the time taken to process the request.
     * The order of this filter is set to the highest precedence, so it will catch any exceptions that occur
     * in other filters before they reach the application's endpoints.
     * @param requestLoggingMiddleware the request logging middleware to register
     * @return a filter registration bean for the request logging middleware
     */
    @Bean
    public FilterRegistrationBean<RequestLoggingMiddleware> requestLoggingFilter() {
        var registrationBean = new FilterRegistrationBean<RequestLoggingMiddleware>();
        registrationBean.setFilter(requestLoggingMiddleware);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }
    
    /**
     * Registers the Redis caching middleware as a filter that caches responses to "/api/*" endpoint requests.
     * This middleware caches responses in Redis for 30 seconds, and will return a cached response if the same
     * request is made within the cache time.
     * The order of this filter is set to the highest precedence, so it will catch any exceptions that occur
     * in other filters before they reach the application's endpoints.
     * @return a filter registration bean for the Redis caching middleware
     */
    @Bean
    public FilterRegistrationBean<RedisCachingMiddleware> redisCachingFilter() {
        FilterRegistrationBean<RedisCachingMiddleware> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(redisCachingMiddleware);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registrationBean;
    }
}