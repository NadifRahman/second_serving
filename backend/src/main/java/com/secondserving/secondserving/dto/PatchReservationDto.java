package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.Reservation;
import jakarta.validation.constraints.Min;

import static com.secondserving.secondserving.domain.Reservation.MIN_QUANTITY_REQUESTABLE;

public record PatchReservationDto(
        Reservation.ReservationStatus reservationStatus,
        @Min(MIN_QUANTITY_REQUESTABLE)
        Short quantityRequested
) {
}
