package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AuthException {

    public InvalidCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
