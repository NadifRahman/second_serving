package com.secondserving.secondserving.exception;

/**
 * Exception thrown when attempting to find a reservation that does not exist in the database.
 */
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}
