package com.vehicleinspection.exception;

/** Thrown when a workflow action is not allowed in the current status (HTTP 409). */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
