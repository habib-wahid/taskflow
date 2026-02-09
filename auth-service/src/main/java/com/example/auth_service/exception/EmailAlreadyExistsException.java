package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + maskEmail(email), HttpStatus.CONFLICT);
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
