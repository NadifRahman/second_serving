package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.FoodListingDto;
import com.secondserving.secondserving.service.FoodListingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FoodListingControllerTest {

    @Test
    void getNearbyFoodListings_returnsNearbyListingDtosAndMapsCoordinates() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        FoodListingController underTest = new FoodListingController(foodListingService);
        User owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        FoodListing listing = new FoodListing(
                owner,
                "Bread",
                "Fresh bread",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 2,
                FoodListing.QuantityUnit.ITEM,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        foodListingService.nearbyListingsToReturn = List.of(listing);

        ResponseEntity<List<FoodListingDto>> response = underTest.getNearbyFoodListings(
                43.6532,
                -79.3832,
                1000.0
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<FoodListingDto> body = assertInstanceOf(List.class, response.getBody());
        assertEquals(1, body.size());
        assertEquals(listing.getListingId(), body.get(0).listingId());
        assertEquals(-79.3832, foodListingService.longitudePassedToFindNearby, 0.0001);
        assertEquals(43.6532, foodListingService.latitudePassedToFindNearby, 0.0001);
        assertEquals(1000.0, foodListingService.radiusPassedToFindNearby, 0.0001);
    }

    @Test
    void getFoodListing_returnsListingDto() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        FoodListingController underTest = new FoodListingController(foodListingService);
        User owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        FoodListing listing = new FoodListing(
                owner,
                "Soup",
                "Fresh soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 3,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        UUID listingId = listing.getListingId();
        foodListingService.listingToReturn = listing;

        ResponseEntity<FoodListingDto> response = underTest.getFoodListing(listingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(listingId, response.getBody().listingId());
        assertEquals(listingId, foodListingService.listingIdPassedToGet);
    }

    private static class StubFoodListingService extends FoodListingService {
        private List<FoodListing> nearbyListingsToReturn = List.of();
        private FoodListing listingToReturn;
        private double longitudePassedToFindNearby;
        private double latitudePassedToFindNearby;
        private double radiusPassedToFindNearby;
        private UUID listingIdPassedToGet;

        StubFoodListingService() {
            super(null, null);
        }

        @Override
        public List<FoodListing> findNearby(double longitude, double latitude, double radiusMeters) {
            longitudePassedToFindNearby = longitude;
            latitudePassedToFindNearby = latitude;
            radiusPassedToFindNearby = radiusMeters;
            return nearbyListingsToReturn;
        }

        @Override
        public FoodListing getFoodListingByIdOrThrow(UUID listingId) {
            listingIdPassedToGet = listingId;
            return listingToReturn;
        }
    }
}
