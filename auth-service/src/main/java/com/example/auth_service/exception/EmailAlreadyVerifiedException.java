package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyVerifiedException extends AuthException {

    public EmailAlreadyVerifiedException() {
        super("Email is already verified", HttpStatus.BAD_REQUEST);
    }
}
