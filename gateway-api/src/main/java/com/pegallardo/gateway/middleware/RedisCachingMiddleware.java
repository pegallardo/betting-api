package com.pegallardo.gateway.middleware;

import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(3)
public class RedisCachingMiddleware extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCachingMiddleware(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // TODO: Implement caching logic here
        
        String url = request.getRequestURI();
        String method = request.getMethod();

        // TODO: proper Cache authentication responses
        if (method.equals("POST") && url.equals("/api/authenticate")) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            // Check if authentication result is cached
            String cacheKey = "auth:" + username;
            if (redisTemplate.hasKey(cacheKey)) {
                // Return cached authentication result
                Object cachedResponse = redisTemplate.opsForValue().get(cacheKey);
                response.getWriter().write(cachedResponse.toString());
                return;
            }
        }

        // If no cache found, proceed with the request
        filterChain.doFilter(request, response);

        // Cache the response
        if (method.equals("POST") && url.equals("/api/authenticate")) {
            String responseBody = response.getWriter().toString();
            String username = request.getParameter("username");
            String cacheKey = "auth:" + username;
            redisTemplate.opsForValue().set(cacheKey, responseBody);
        }
    }
}