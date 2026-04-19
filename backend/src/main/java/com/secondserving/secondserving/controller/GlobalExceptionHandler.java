package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.exception.DuplicateReservationException;
import com.secondserving.secondserving.exception.FoodListingNotFoundException;
import com.secondserving.secondserving.exception.InvalidReservationQuantityException;
import com.secondserving.secondserving.exception.SelfReservationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The global exception handler for controller classes. All the methods will return a single string that can be
 * used by clients to show to their users.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FoodListingNotFoundException.class)
    public ResponseEntity<String> handleFoodListingNotFound(FoodListingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateReservationException.class)
    public ResponseEntity<String> handleDuplicateReservation(DuplicateReservationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidReservationQuantityException.class)
    public ResponseEntity<String> handleInvalidReservationQuantity(InvalidReservationQuantityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SelfReservationException.class)
    public ResponseEntity<String> handleSelfReservation(SelfReservationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
