package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.Reservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationDTO(
        UUID listingId,
        UUID requesterId,
        String requesterUsername,
        short quantityRequested,
        Reservation.ReservationStatus reservationStatus,
        Instant createdAt,
        Instant updatedAt,
        FoodListingDto foodListing
) {
    public static ReservationDTO from(Reservation reservation) {
        return new ReservationDTO(
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
