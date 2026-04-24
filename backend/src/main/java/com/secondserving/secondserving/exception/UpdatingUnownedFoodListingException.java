package com.secondserving.secondserving.exception;

/**
 * Exception thrown if an attempt is made to update OR delete a foodlisting by a user that does not own it
 */
public class UpdatingUnownedFoodListingException extends RuntimeException {
    public UpdatingUnownedFoodListingException(String message) {
        super(message);
    }
}
