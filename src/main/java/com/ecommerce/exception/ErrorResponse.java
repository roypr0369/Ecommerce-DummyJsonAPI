package com.ecommerce.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Consistent JSON error body returned for every API error.
 *
 * All error responses look like:
 * {
 *   "status":    404,
 *   "error":     "Not Found",
 *   "message":   "Product not found with id: '99'",
 *   "path":      "/products/99",
 *   "timestamp": "2024-03-01T10:30:00"
 * }
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus()               { return status; }
    public String getError()             { return error; }
    public String getMessage()           { return message; }
    public String getPath()              { return path; }
    public LocalDateTime getTimestamp()  { return timestamp; }
}
