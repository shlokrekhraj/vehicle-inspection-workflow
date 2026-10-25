package com.vehicleinspection.exception;

/** Thrown when login credentials are wrong (HTTP 401). */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
