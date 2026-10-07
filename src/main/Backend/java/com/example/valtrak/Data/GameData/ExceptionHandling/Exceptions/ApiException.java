package com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions;

import org.springframework.http.HttpStatus;

/**
 * An error with a specific HTTP status whose message is safe to show to the user.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
