package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.PickupLocation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.exception.UpdatingUnownedFoodListingException;
import com.secondserving.secondserving.repository.FoodListingRepository;
import com.secondserving.secondserving.repository.PickupLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodListingServiceTest {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private PickupLocationRepository pickupLocationRepository;

    @InjectMocks
    private FoodListingService foodListingService;

    private User owner;
    private FoodListing listing;
    private Point point;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        point = GEOMETRY_FACTORY.createPoint(new Coordinate(-79.3832, 43.6532));
        listing = new FoodListing(
                owner,
                "Soup",
                "Fresh vegetable soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 3,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-04-19T12:00:00Z")
        );
    }

    @Test
    void createListing_WithPickupLocation_SavesListingAggregate() {
        FoodListingService.CreateFoodListingCommand command = new FoodListingService.CreateFoodListingCommand(
                "Soup",
                "Fresh vegetable soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 3,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-04-19T12:00:00Z"),
                new FoodListingService.PickupLocationCommand(
                        "123 Queen St W",
                        point,
                        Instant.parse("2026-04-19T13:00:00Z"),
                        Instant.parse("2026-04-19T15:00:00Z"),
                        "Ring the bell"
                )
        );

        when(foodListingRepository.save(any(FoodListing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoodListing created = foodListingService.createListing(owner, command);

        assertNotNull(created.getPickupLocation());
        assertSame(created, created.getPickupLocation().getFoodListing());
        assertEquals("123 Queen St W", created.getPickupLocation().getFullAddress());
        verify(foodListingRepository).save(created);
    }

    @Test
    void updateListing_UpdatesMutableFields() {
        UUID listingId = listing.getListingId();
        FoodListingService.PatchFoodListingCommand command = new FoodListingService.PatchFoodListingCommand(
                FoodListing.FoodListingStatus.FINISHED,
                (short) 1,
                Instant.parse("2026-04-20T12:00:00Z")
        );

        when(foodListingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(foodListingRepository.save(listing)).thenReturn(listing);

        FoodListing updated = foodListingService.patchListingIfOwnedByUserOrThrow(owner, listingId, command);

        assertEquals(FoodListing.FoodListingStatus.FINISHED, updated.getStatus());
        assertEquals((short) 1, updated.getQuantity());
        assertEquals(Instant.parse("2026-04-20T12:00:00Z"), updated.getExpiresAt());
    }

    @Test
    void patchListingIfOwnedByUserOrThrow_WithOnlyQuantity_LeavesOtherPatchableFieldsUnchanged() {
        UUID listingId = listing.getListingId();
        FoodListingService.PatchFoodListingCommand command = new FoodListingService.PatchFoodListingCommand(
                null,
                (short) 1,
                null
        );

        when(foodListingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(foodListingRepository.save(listing)).thenReturn(listing);

        FoodListing updated = foodListingService.patchListingIfOwnedByUserOrThrow(owner, listingId, command);

        assertEquals(FoodListing.FoodListingStatus.AVAILABLE, updated.getStatus());
        assertEquals((short) 1, updated.getQuantity());
        assertEquals(Instant.parse("2026-04-19T12:00:00Z"), updated.getExpiresAt());
    }

    @Test
    void patchListingIfOwnedByUserOrThrow_WithDifferentUser_ThrowsUpdatingUnownedFoodListingException() {
        UUID listingId = listing.getListingId();
        User otherUser = new User("other", "passwordHash", "Other User", "other@example.com");
        FoodListingService.PatchFoodListingCommand command = new FoodListingService.PatchFoodListingCommand(
                FoodListing.FoodListingStatus.FINISHED,
                null,
                null
        );

        when(foodListingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        UpdatingUnownedFoodListingException exception = assertThrows(UpdatingUnownedFoodListingException.class,
                () -> foodListingService.patchListingIfOwnedByUserOrThrow(otherUser, listingId, command));

        assertEquals("You cannot update a food listing owned by another user.", exception.getMessage());
    }

    @Test
    void updatePickupLocation_CreatesPickupLocationWhenMissing() {
        UUID listingId = listing.getListingId();
        FoodListingService.PickupLocationCommand command = new FoodListingService.PickupLocationCommand(
                "123 Queen St W",
                point,
                Instant.parse("2026-04-19T13:00:00Z"),
                Instant.parse("2026-04-19T15:00:00Z"),
                "Side entrance"
        );

        when(foodListingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(foodListingRepository.save(listing)).thenReturn(listing);

        FoodListing updated = foodListingService.updatePickupLocation(listingId, command);

        assertNotNull(updated.getPickupLocation());
        assertEquals("Side entrance", updated.getPickupLocation().getInstructions());
        assertSame(updated, updated.getPickupLocation().getFoodListing());
    }

    @Test
    void findNearby_ReturnsAssociatedFoodListings() {
        PickupLocation pickupLocation = new PickupLocation(
                listing,
                "123 Queen St W",
                point,
                Instant.parse("2026-04-19T13:00:00Z"),
                Instant.parse("2026-04-19T15:00:00Z"),
                "Front desk"
        );

        when(pickupLocationRepository.findNearby(-79.3832, 43.6532, 1000)).thenReturn(List.of(pickupLocation));

        List<FoodListing> nearbyListings = foodListingService.findNearby(-79.3832, 43.6532, 1000);

        assertEquals(1, nearbyListings.size());
        assertSame(listing, nearbyListings.get(0));
    }

    @Test
    void getFoodListingsOwnedBy_ReturnsOwnerListings() {
        when(foodListingRepository.findAllByOwner(owner)).thenReturn(List.of(listing));

        List<FoodListing> ownedListings = foodListingService.getFoodListingsOwnedBy(owner);

        assertEquals(1, ownedListings.size());
        assertSame(listing, ownedListings.get(0));
    }

    @Test
    void createListing_ThrowsWhenPickupLocationIsMissing() {
        FoodListingService.CreateFoodListingCommand command = new FoodListingService.CreateFoodListingCommand(
                "Soup",
                "Fresh vegetable soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 3,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-04-19T12:00:00Z"),
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> foodListingService.createListing(owner, command));

        assertEquals("Pickup location is required", exception.getMessage());
    }
}
