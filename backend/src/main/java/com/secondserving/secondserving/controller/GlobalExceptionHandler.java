package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Converts exceptions thrown from controller workflows into stable HTTP responses.
 * <p>
 * The current API error contract is intentionally simple: error responses are returned as {@code text/plain} strings
 * that can be displayed by clients. Springdoc does not automatically attach these global handler responses to each
 * endpoint in Swagger UI, so {@link com.secondserving.secondserving.config.OpenApiConfig} documents the shared error
 * responses in the generated OpenAPI contract.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles attempts to read, update, or delete a food listing that does not exist.
     *
     * @param ex the domain exception containing the client-facing message
     * @return a {@code 404 Not Found} plain-text response
     */
    @ExceptionHandler(FoodListingNotFoundException.class)
    public ResponseEntity<String> handleFoodListingNotFound(FoodListingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles attempts to read, update, or delete a reservation that does not exist.
     *
     * @param ex the domain exception containing the client-facing message
     * @return a {@code 404 Not Found} plain-text response
     */
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<String> handleReservationNotFound(ReservationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles attempts to create a reservation that already exists for the same requester and listing.
     *
     * @param ex the domain exception containing the client-facing message
     * @return a {@code 409 Conflict} plain-text response
     */
    @ExceptionHandler(DuplicateReservationException.class)
    public ResponseEntity<String> handleDuplicateReservation(DuplicateReservationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Handles reservation requests whose quantity is not allowed by the listing or reservation rules.
     *
     * @param ex the domain exception containing the client-facing message
     * @return a {@code 400 Bad Request} plain-text response
     */
    @ExceptionHandler(InvalidReservationQuantityException.class)
    public ResponseEntity<String> handleInvalidReservationQuantity(InvalidReservationQuantityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles attempts by a user to reserve one of their own food listings.
     *
     * @param ex the domain exception containing the client-facing message
     * @return a {@code 400 Bad Request} plain-text response
     */
    @ExceptionHandler(SelfReservationException.class)
    public ResponseEntity<String> handleSelfReservation(SelfReservationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles attempts to update or delete a food listing owned by another user.
     *
     * @param ex the domain exception containing the client-facing message
     * @return a {@code 403 Forbidden} plain-text response
     */
    @ExceptionHandler(UpdatingUnownedFoodListingException.class)
    public ResponseEntity<String> handleUpdatingUnownedFoodListing(UpdatingUnownedFoodListingException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Handles invalid username/password attempts during login.
     *
     * @param ex the Spring Security exception containing the client-facing message
     * @return a {@code 401 Unauthorized} plain-text response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Handles invalid request state that does not have a more specific domain exception type yet.
     *
     * @param ex the exception containing the client-facing message
     * @return a {@code 400 Bad Request} plain-text response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles Jakarta validation failures from request DTOs.
     * <p>
     * Field-level validation errors are joined into a single plain-text message, for example
     * {@code title: must not be blank, quantity: must be greater than or equal to 1}.
     *
     * @param ex the Spring MVC exception containing request DTO validation errors
     * @return a {@code 400 Bad Request} plain-text response listing invalid fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationFailure(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
