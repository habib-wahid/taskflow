package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("User not found", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
