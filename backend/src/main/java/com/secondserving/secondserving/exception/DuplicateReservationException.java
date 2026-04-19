package com.secondserving.secondserving.exception;

/**
 * Exception thrown when there is an attempt to create a reservation for a particular user on a particular
 * food listing and one already currently exists.
 */
public class DuplicateReservationException extends RuntimeException {
    public DuplicateReservationException(String message) {
        super(message);
    }
}
