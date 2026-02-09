package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends AuthException {

    public AccountLockedException() {
        super("Account is temporarily locked due to too many failed login attempts", HttpStatus.UNAUTHORIZED);
    }

    public AccountLockedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
