package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.Reservation;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO class that contains all the details of a particular reservation. Useful when we need to transfer data
 * of a reservation and we do not already know its related entities.
 */
public record ReservationDto(
        UUID listingId,
        UUID requesterId,
        String requesterUsername,
        short quantityRequested,
        Reservation.ReservationStatus reservationStatus,
        Instant createdAt,
        Instant updatedAt,
        FoodListingDto foodListing
) {
    public static ReservationDto from(Reservation reservation) {
        return new ReservationDto(
                reservation.getReservationId().getListingId(),
                reservation.getReservationId().getRequesterId(),
                reservation.getUserRequester().getUsername(),
                reservation.getQuantityRequested(),
                reservation.getReservationStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                FoodListingDto.from(reservation.getFoodListing())
        );
    }
}
