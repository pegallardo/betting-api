package com.pegallardo.gateway.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(2)
public class RequestLoggingMiddleware extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingMiddleware.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException, ServletException {
        var requestWrapper = new ContentCachingRequestWrapper(request);
        var responseWrapper = new ContentCachingResponseWrapper(response);
        var requestId = UUID.randomUUID().toString();

        MDC.put("requestId", requestId);
        logger.info("Incoming Request: {}", requestWrapper.getRequestURI());

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            logOutgoingResponse(responseWrapper, requestId);
        } finally {
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logOutgoingResponse(ContentCachingResponseWrapper response, String requestId) {
        logger.info("Outgoing Response: {}", response.getStatus());
        logger.info("Response Headers: {}", response.getHeaderNames());
        logger.info("Response Body: {}", new String(response.getContentAsByteArray()));
    }
}