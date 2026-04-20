package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.Reservation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO that holds that data needed to make a reservation on a particular food listing, assuming we already have the user
 * that makes this reservation.
 * @param listingId the unique identifer of the {@link FoodListing} to make a reservation on.
 * @param quantityRequested The quantity that this reservation wants to make.
 */
public record CreateReservationRequestDto(
        @NotNull
        UUID listingId,

        @NotNull
        @Min(Reservation.MIN_QUANTITY_REQUESTABLE)
        Short quantityRequested
) {
}
