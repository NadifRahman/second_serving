package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.Reservation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.exception.DuplicateReservationException;
import com.secondserving.secondserving.exception.FoodListingNotFoundException;
import com.secondserving.secondserving.exception.InvalidReservationQuantityException;
import com.secondserving.secondserving.exception.SelfReservationException;
import com.secondserving.secondserving.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private FoodListingService foodListingService;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User owner;
    private User requester;
    private FoodListing listing;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        requester = new User("requester", "passwordHash", "Requester Name", "requester@example.com");
        listing = new FoodListing(
                owner,
                "Soup",
                "Fresh soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 4,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-04-19T12:00:00Z")
        );
        reservation = new Reservation(
                listing,
                requester,
                (short) 2,
                Reservation.ReservationStatus.REQUESTED
        );
    }

    @Test
    void createReservation_SavesRequestedReservation() {
        UUID listingId = listing.getListingId();

        when(foodListingService.getFoodListingByIdOrThrow(listingId)).thenReturn(listing);
        when(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation created = reservationService.createReservation(requester, listingId, (short) 2);

        assertSame(listing, created.getFoodListing());
        assertSame(requester, created.getUserRequester());
        assertEquals((short) 2, created.getQuantityRequested());
        assertEquals(Reservation.ReservationStatus.REQUESTED, created.getReservationStatus());
    }

    @Test
    void createReservation_ThrowsWhenListingDoesNotExist() {
        UUID listingId = UUID.randomUUID();

        when(foodListingService.getFoodListingByIdOrThrow(listingId))
                .thenThrow(new FoodListingNotFoundException("Could not find food listing with id " + listingId));

        FoodListingNotFoundException exception = assertThrows(FoodListingNotFoundException.class,
                () -> reservationService.createReservation(requester, listingId, (short) 2));

        assertEquals("Could not find food listing with id " + listingId, exception.getMessage());
    }

    @Test
    void createReservation_ThrowsWhenUserReservesOwnListing() {
        UUID listingId = listing.getListingId();

        when(foodListingService.getFoodListingByIdOrThrow(listingId)).thenReturn(listing);

        SelfReservationException exception = assertThrows(SelfReservationException.class,
                () -> reservationService.createReservation(owner, listingId, (short) 2));

        assertEquals("Users cannot reserve their own food listing", exception.getMessage());
    }

    @Test
    void createReservation_ThrowsWhenQuantityRequestedIsNegative() {
        UUID listingId = listing.getListingId();

        when(foodListingService.getFoodListingByIdOrThrow(listingId)).thenReturn(listing);

        InvalidReservationQuantityException exception = assertThrows(InvalidReservationQuantityException.class,
                () -> reservationService.createReservation(requester, listingId, (short) -1));

        assertEquals("Quantity requested is below minimum of " + Reservation.MIN_QUANTITY_REQUESTABLE, exception.getMessage());
    }

    @Test
    void createReservation_ThrowsWhenReservationAlreadyExists() {
        UUID listingId = listing.getListingId();

        when(foodListingService.getFoodListingByIdOrThrow(listingId)).thenReturn(listing);
        when(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        DuplicateReservationException exception = assertThrows(DuplicateReservationException.class,
                () -> reservationService.createReservation(requester, listingId, (short) 2));

        assertEquals("Reservation already exists for this user and listing", exception.getMessage());
    }

    @Test
    void getReservationsRequestedBy_UsesDetailedRepositoryQuery() {
        when(reservationRepository.findDetailedByUserRequester(requester)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getReservationsRequestedBy(requester);

        assertEquals(1, result.size());
        assertSame(reservation, result.get(0));
        verify(reservationRepository).findDetailedByUserRequester(requester);
    }

    @Test
    void getReservationsForFoodListing_UsesDetailedRepositoryQuery() {
        when(reservationRepository.findDetailedByFoodListing(listing)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getReservationsForFoodListing(listing);

        assertEquals(1, result.size());
        assertSame(reservation, result.get(0));
        verify(reservationRepository).findDetailedByFoodListing(listing);
    }
}
