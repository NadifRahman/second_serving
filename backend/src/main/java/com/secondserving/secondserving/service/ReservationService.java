package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.Reservation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.exception.DuplicateReservationException;
import com.secondserving.secondserving.exception.InvalidReservationQuantityException;
import com.secondserving.secondserving.exception.SelfReservationException;
import com.secondserving.secondserving.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.secondserving.secondserving.domain.Reservation.MIN_QUANTITY_REQUESTABLE;

@Service
@Transactional
public class ReservationService {

    private final FoodListingService foodListingService;
    private final ReservationRepository reservationRepository;

    public ReservationService(FoodListingService foodListingService,
                              ReservationRepository reservationRepository) {
        this.foodListingService = foodListingService;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Creates a new reservation request for the given listing on behalf of the requester.
     *
     * @param requester The user making the reservation
     * @param listingId The target food listing id
     * @param quantityRequested The quantity being requested. This should be less than the food listing quantity available.
     * @return The saved reservation
     */
    public Reservation createReservation(User requester, UUID listingId, short quantityRequested) {
        FoodListing foodListing = foodListingService.getFoodListingByIdOrThrow(listingId);

        if (foodListing.getOwner().getUserId().equals(requester.getUserId())) {
            throw new SelfReservationException("Users cannot reserve their own food listing");
        }

        if (quantityRequested < MIN_QUANTITY_REQUESTABLE) {
            throw new InvalidReservationQuantityException("Quantity requested is below minimum of " +  MIN_QUANTITY_REQUESTABLE);
        }

        if (quantityRequested > foodListing.getQuantity()) {
            throw new InvalidReservationQuantityException("Quantity requested is above available quantity " + foodListing.getQuantity());
        }

        Reservation reservation = new Reservation(
                foodListing,
                requester,
                quantityRequested,
                Reservation.ReservationStatus.REQUESTED
        );

        try {
            return reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            // TODO re-evaluate this...perhaps not best way to do it. We catch all data constraint problems here
            throw new DuplicateReservationException("Reservation already exists for this user and listing");
        }

    }

    /**
     * Fetches reservations requested by a given user with related entities loaded.
     *
     * @param requester The requesting user
     * @return The user's reservations
     */
    public List<Reservation> getReservationsRequestedBy(User requester) {
        return reservationRepository.findDetailedByUserRequester(requester);
    }

    /**
     * Fetches reservations made against a food listing with related entities loaded.
     *
     * @param foodListing The food listing
     * @return Reservations for the listing
     */
    public List<Reservation> getReservationsForFoodListing(FoodListing foodListing) {
        return reservationRepository.findDetailedByFoodListing(foodListing);
    }
}
