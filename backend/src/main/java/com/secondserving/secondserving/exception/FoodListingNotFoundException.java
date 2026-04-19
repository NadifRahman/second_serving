package com.secondserving.secondserving.exception;

/**
 * Exception thrown when attempting to find a food listing that does not exist in the database
 */
public class FoodListingNotFoundException extends RuntimeException {
    public FoodListingNotFoundException(String s) {
        super(s);
    }
}
