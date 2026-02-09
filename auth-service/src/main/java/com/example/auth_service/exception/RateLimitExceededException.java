package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends AuthException {

    public RateLimitExceededException() {
        super("Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }

    public RateLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
