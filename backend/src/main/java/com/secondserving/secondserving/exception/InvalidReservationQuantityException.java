package com.secondserving.secondserving.exception;

/**
 * Exception that should be thrown when there is an attempt to create a reservation with a quantity that is invalid
 */
public class InvalidReservationQuantityException extends RuntimeException {
    public InvalidReservationQuantityException(String message) {
        super(message);
    }
}
