package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.TestUtils.PostgisTestContainerConfig;
import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.Reservation;
import com.secondserving.secondserving.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ReservationRepository}.
 *
 * <p>These tests use a PostGIS-backed Testcontainers setup so that query behavior is validated against a real PostgreSQL schema created by
 * Flyway migrations.
 *
 * <p>Reservations are persisted through the repository itself so these tests exercise the
 * actual entity mapping, including the composite identifier built from foreign keys.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(PostgisTestContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository underTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * Verifies that filtering by requester returns only reservations owned by that user,
     * even when other users have reservations for the same listing.
     */
    @Test
    void findByUserRequester_returnsReservationsForThatUserOnly() {
        User owner = saveUser("owner-one", "owner1@example.com");
        User requester = saveUser("requester-one", "requester1@example.com");
        User otherRequester = saveUser("requester-two", "requester2@example.com");

        FoodListing listingOne = saveListing(owner, "Listing One");
        FoodListing listingTwo = saveListing(owner, "Listing Two");

        saveReservation(listingOne, requester, (short) 2, Reservation.ReservationStatus.REQUESTED);
        saveReservation(listingTwo, requester, (short) 1, Reservation.ReservationStatus.CANCELLED);
        saveReservation(listingOne, otherRequester, (short) 3, Reservation.ReservationStatus.REQUESTED);

        entityManager.flush();
        entityManager.clear();

        List<Reservation> reservations = underTest.findByUserRequester(requester);

        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r ->
                r.getReservationId().getRequesterId().equals(requester.getUserId())));
    }

    /**
     * Verifies that filtering by food listing returns only reservations for that listing,
     * even when the same requester has reservations on a different listing.
     */
    @Test
    void findByFoodListing_returnsReservationsForThatListingOnly() {
        User owner = saveUser("owner-two", "owner2@example.com");
        User requesterOne = saveUser("requester-three", "requester3@example.com");
        User requesterTwo = saveUser("requester-four", "requester4@example.com");

        FoodListing targetListing = saveListing(owner, "Target Listing");
        FoodListing otherListing = saveListing(owner, "Other Listing");

        saveReservation(targetListing, requesterOne, (short) 1, Reservation.ReservationStatus.REQUESTED);
        saveReservation(targetListing, requesterTwo, (short) 4, Reservation.ReservationStatus.COLLECTED);
        saveReservation(otherListing, requesterOne, (short) 2, Reservation.ReservationStatus.REQUESTED);

        entityManager.flush();
        entityManager.clear();

        List<Reservation> reservations = underTest.findByFoodListing(targetListing);

        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().allMatch(r ->
                r.getReservationId().getListingId().equals(targetListing.getListingId())));
    }

    /**
     * Confirms that the non-detailed repository method preserves the entity's default
     * lazy-loading behavior for both associated entities.
     */
    @Test
    void findByFoodListing_keepsAssociationsLazy() {
        User owner = saveUser("owner-three", "owner3@example.com");
        User requester = saveUser("requester-five", "requester5@example.com");
        FoodListing listing = saveListing(owner, "Lazy Listing");

        saveReservation(listing, requester, (short) 1, Reservation.ReservationStatus.REQUESTED);

        entityManager.flush();
        entityManager.clear();

        List<Reservation> reservations = underTest.findByFoodListing(listing);
        Reservation reservation = reservations.get(0);
        PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertFalse(persistenceUnitUtil.isLoaded(reservation, "foodListing"));
        assertFalse(persistenceUnitUtil.isLoaded(reservation, "userRequester"));
    }

    /**
     * Confirms that the detailed food-listing query eagerly loads both associated entities
     * through the repository's {@code @EntityGraph} configuration.
     */
    @Test
    void findDetailedByFoodListing_eagerlyLoadsAssociations() {
        User owner = saveUser("owner-four", "owner4@example.com");
        User requester = saveUser("requester-six", "requester6@example.com");
        FoodListing listing = saveListing(owner, "Detailed Listing");

        saveReservation(listing, requester, (short) 5, Reservation.ReservationStatus.REQUESTED);

        entityManager.flush();
        entityManager.clear();

        List<Reservation> reservations = underTest.findDetailedByFoodListing(listing);
        Reservation reservation = reservations.get(0);
        PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(persistenceUnitUtil.isLoaded(reservation, "foodListing"));
        assertTrue(persistenceUnitUtil.isLoaded(reservation, "userRequester"));
    }

    /**
     * Confirms that the detailed requester query eagerly loads both associated entities
     * through the repository's {@code @EntityGraph} configuration.
     */
    @Test
    void findDetailedByUserRequester_eagerlyLoadsAssociations() {
        User owner = saveUser("owner-five", "owner5@example.com");
        User requester = saveUser("requester-seven", "requester7@example.com");
        FoodListing listing = saveListing(owner, "Requester Detailed Listing");

        saveReservation(listing, requester, (short) 2, Reservation.ReservationStatus.REQUESTED);

        entityManager.flush();
        entityManager.clear();

        List<Reservation> reservations = underTest.findDetailedByUserRequester(requester);
        Reservation reservation = reservations.get(0);
        PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(persistenceUnitUtil.isLoaded(reservation, "foodListing"));
        assertTrue(persistenceUnitUtil.isLoaded(reservation, "userRequester"));
    }

    /**
     * Persists a user with minimal valid values for repository testing.
     */
    private User saveUser(String username, String email) {
        return userRepository.saveAndFlush(new User(
                username,
                "hashed-password",
                username + " Fullname",
                email
        ));
    }

    /**
     * Persists a valid food listing owned by the supplied user.
     */
    private FoodListing saveListing(User owner, String title) {
        return foodListingRepository.saveAndFlush(new FoodListing(
                owner,
                title,
                title + " description",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 10,
                FoodListing.QuantityUnit.ITEM,
                Instant.now().plusSeconds(3600)
        ));
    }

    /**
     * Persists a reservation using the production entity constructor and repository mapping.
     */
    private Reservation saveReservation(FoodListing foodListing,
                                        User userRequester,
                                        short quantityRequested,
                                        Reservation.ReservationStatus status) {
        return underTest.saveAndFlush(new Reservation(
                foodListing,
                userRequester,
                quantityRequested,
                status
        ));
    }
}
