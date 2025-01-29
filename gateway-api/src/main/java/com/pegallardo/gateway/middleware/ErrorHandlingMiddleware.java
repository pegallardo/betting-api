package com.pegallardo.gateway.middleware;

import com.pegallardo.gateway.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Order(1)
public class ErrorHandlingMiddleware extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingMiddleware.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException {
        var requestWrapper = new ContentCachingRequestWrapper(request);
        var responseWrapper = new ContentCachingResponseWrapper(response);
        var errorRef = new AtomicReference<Throwable>();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            handlePossibleError(requestWrapper, responseWrapper);
        } catch (Exception e) {
            errorRef.set(e);
            handleException(requestWrapper, responseWrapper, e);
        } finally {
            responseWrapper.copyBodyToResponse();
            logError(errorRef.get(), requestWrapper);
        }
    }

    private void handlePossibleError(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) throws IOException {
        if (response.getStatus() >= 400) {
            var errorResponse = new ErrorResponse(
                    response.getStatus(),
                    request.getRequestURI(),
                    HttpStatus.valueOf(response.getStatus()).getReasonPhrase(),
                    "",
                    Instant.now(),
                    request.getHeader("X-Trace-ID")
            );

            writeErrorResponse(response, errorResponse);
        }
    }

    private void handleException(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, Exception ex) throws IOException {
        var errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                "Internal Server Error",
                ex.getMessage(),
                Instant.now(),
                request.getHeader("X-Trace-ID")
        );

        writeErrorResponse(response, errorResponse);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(errorResponse.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\": " + errorResponse.getStatus() +
                ", \"path\": \"" + errorResponse.getPath() +
                "\", \"error\": \"" + errorResponse.getError() +
                "\", \"message\": \"" + errorResponse.getMessage() +
                "\", \"timestamp\": \"" + errorResponse.getTimestamp() +
                "\", \"traceId\": \"" + errorResponse.getTraceId() + "\"}"
        );
    }

    private void logError(Throwable error, HttpServletRequest request) {
        if (error != null) {
            logger.error("Request {} {} failed: {}", request.getMethod(), request.getRequestURI(), error.getMessage(), error);
        }
    }
}