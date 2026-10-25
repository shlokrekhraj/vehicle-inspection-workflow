package com.vehicleinspection.exception;

import java.util.List;

/** Thrown when submitted data fails validation (HTTP 400). */
public class InvalidRequestException extends RuntimeException {

    private final List<String> errors;

    public InvalidRequestException(List<String> errors) {
        super("Invalid inspection request data");
        this.errors = List.copyOf(errors);
    }

    public InvalidRequestException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public List<String> getErrors() {
        return errors;
    }
}
