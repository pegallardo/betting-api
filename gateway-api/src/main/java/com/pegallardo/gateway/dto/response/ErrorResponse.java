package com.pegallardo.gateway.dto.response;

import java.time.Instant;

public class ErrorResponse {
    private int status;
    private String path;
    private String error;
    private String message;
    private Instant timestamp;
    private String traceId;

    public ErrorResponse(int status, String path, String error, String message, Instant timestamp, String traceId) {
        this.status = status;
        this.path = path;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.traceId = traceId;
    }

    // Getters and setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}