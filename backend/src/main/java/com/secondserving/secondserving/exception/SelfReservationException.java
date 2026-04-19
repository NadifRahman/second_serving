package com.secondserving.secondserving.exception;

/**
 * Exception that is thrown if a user attempts to make a reservation on a food listing owned by themselves.
 */
public class SelfReservationException extends RuntimeException {
    public SelfReservationException(String message) {
        super(message);
    }
}
