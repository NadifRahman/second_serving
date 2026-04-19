package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.Reservation;
import com.secondserving.secondserving.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Reservation.ReservationPK> {

    /**
     * Finds reservations requested by the given user.
     * Related {@code foodListing} and {@code userRequester} associations remain lazily loaded.
     *
     * @param userRequester the requesting user
     * @return reservations requested by the given user
     */
    public List<Reservation> findByUserRequester(User userRequester);

    /**
     * Finds reservations for the given food listing.
     * Related {@code foodListing} and {@code userRequester} associations remain lazily loaded.
     *
     * @param foodListing the food listing whose reservations should be returned
     * @return reservations for the given food listing
     */
    public List<Reservation> findByFoodListing(FoodListing foodListing);

    /**
     * Finds reservations for the given food listing and eagerly loads both
     * {@code foodListing} and {@code userRequester}.
     *
     * @param foodListing the food listing whose reservations should be returned
     * @return reservations for the given food listing with related entities loaded
     */
    @EntityGraph(attributePaths = {"foodListing", "userRequester"})
    List<Reservation> findDetailedByFoodListing(FoodListing foodListing);

    /**
     * Finds reservations requested by the given user and eagerly loads both
     * {@code foodListing} and {@code userRequester}.
     *
     * @param userRequester the requesting user
     * @return reservations requested by the given user with related entities loaded
     */
    @EntityGraph(attributePaths = {"foodListing", "userRequester"})
    List<Reservation> findDetailedByUserRequester(User userRequester);

}
