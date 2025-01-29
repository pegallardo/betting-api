package com.pegallardo.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles fallback for unavailable services.
 */
@RestController
public class FallbackController {

    private static final String FALLBACK_MESSAGE = "Service is currently unavailable. Please try again later.";
    private static final HttpStatus FALLBACK_STATUS = HttpStatus.SERVICE_UNAVAILABLE;

    @GetMapping("/fallback")
    public ResponseEntity<String> fallback() {
        return ResponseEntity.status(FALLBACK_STATUS)
                .body(FALLBACK_MESSAGE);
    }
}