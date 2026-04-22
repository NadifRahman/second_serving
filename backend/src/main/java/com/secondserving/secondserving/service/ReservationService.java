package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.Reservation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.exception.*;
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

        validateReservationRequest(requester, foodListing, quantityRequested);

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
     * Patches an existing reservation requested by the given user.
     *
     * @param requester the authenticated user who requested the reservation
     * @param listingId the reserved food listing id
     * @param command the patch fields to apply
     * @return the saved reservation
     * @throws FoodListingNotFoundException if the listing cannot be found
     * @throws ReservationNotFoundException if the authenticated user has no reservation for the listing
     * @throws SelfReservationException if the requester owns the listing
     * @throws InvalidReservationQuantityException if the patched quantity is invalid for the listing
     */
    public Reservation patchReservationRequestedByUserOrThrow(User requester, UUID listingId, PatchReservationCommand command) {
        FoodListing foodListing = foodListingService.getFoodListingByIdOrThrow(listingId);
        Reservation reservation = getReservationRequestedByUserOrThrow(requester, listingId);

        short quantityToValidate = command.quantityRequested() == null
                ? reservation.getQuantityRequested()
                : command.quantityRequested();
        validateReservationRequest(requester, foodListing, quantityToValidate);

        if (command.reservationStatus() != null) {
            reservation.setReservationStatus(command.reservationStatus());
        }
        if (command.quantityRequested() != null) {
            reservation.setQuantityRequested(command.quantityRequested());
        }

        return reservationRepository.save(reservation);
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

    private Reservation getReservationRequestedByUserOrThrow(User requester, UUID listingId) {
        Reservation.ReservationPK reservationId = new Reservation.ReservationPK(listingId, requester.getUserId());
        return reservationRepository.findDetailedByReservationId(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Could not find reservation for listing " + listingId + " requested by user " + requester.getUserId()
                ));
    }

    private void validateReservationRequest(User requester, FoodListing foodListing, short quantityRequested) {
        if (foodListing.getOwner().getUserId().equals(requester.getUserId())) {
            throw new SelfReservationException("Users cannot reserve their own food listing");
        }

        if (quantityRequested < MIN_QUANTITY_REQUESTABLE) {
            throw new InvalidReservationQuantityException("Quantity requested is below minimum of " +  MIN_QUANTITY_REQUESTABLE);
        }

        if (quantityRequested > foodListing.getQuantity()) {
            throw new InvalidReservationQuantityException("Quantity requested is above available quantity " + foodListing.getQuantity());
        }
    }

    /**
     * Input data for patching the mutable fields of an existing {@link Reservation}.
     * Fields should be set to null if they should not be applied to the patch.
     */
    public record PatchReservationCommand(Reservation.ReservationStatus reservationStatus,
                                          Short quantityRequested) {
    }
}
