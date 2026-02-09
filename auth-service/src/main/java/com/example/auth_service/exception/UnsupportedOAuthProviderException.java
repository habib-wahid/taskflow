package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedOAuthProviderException extends AuthException {

    public UnsupportedOAuthProviderException(String provider) {
        super("Unsupported OAuth provider: " + provider, HttpStatus.BAD_REQUEST);
    }
}
