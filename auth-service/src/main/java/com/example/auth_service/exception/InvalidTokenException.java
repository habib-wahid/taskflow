package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public static InvalidTokenException expired() {
        return new InvalidTokenException("Token has expired");
    }

    public static InvalidTokenException invalid() {
        return new InvalidTokenException("Invalid token");
    }

    public static InvalidTokenException revoked() {
        return new InvalidTokenException("Token has been revoked");
    }

    public static InvalidTokenException alreadyUsed() {
        return new InvalidTokenException("Token has already been used");
    }
}
