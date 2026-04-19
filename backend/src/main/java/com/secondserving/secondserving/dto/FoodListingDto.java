package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.PickupLocation;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

public record FoodListingDto(
        UUID listingId,
        UUID ownerId,
        String ownerUsername,
        String title,
        String description,
        FoodListing.FoodListingStatus status,
        short quantity,
        FoodListing.QuantityUnit quantityUnit,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt,
        PickupLocationDto pickupLocation
) {
    public static FoodListingDto from(FoodListing foodListing) {
        return new FoodListingDto(
                foodListing.getListingId(),
                foodListing.getOwner().getUserId(),
                foodListing.getOwner().getUsername(),
                foodListing.getTitle(),
                foodListing.getDescription(),
                foodListing.getStatus(),
                foodListing.getQuantity(),
                foodListing.getQuantityUnit(),
                foodListing.getExpiresAt(),
                foodListing.getCreatedAt(),
                foodListing.getUpdatedAt(),
                PickupLocationDto.from(foodListing.getPickupLocation())
        );
    }

    public record PickupLocationDto(
            UUID pickupId,
            String fullAddress,
            Double longitude,
            Double latitude,
            Instant pickupStartAt,
            Instant pickupEndAt,
            String instructions
    ) {
        public static PickupLocationDto from(PickupLocation pickupLocation) {
            if (pickupLocation == null) {
                return null;
            }

            Point point = pickupLocation.getLocationPoint();

            return new PickupLocationDto(
                    pickupLocation.getPickupId(),
                    pickupLocation.getFullAddress(),
                    point == null ? null : point.getX(),
                    point == null ? null : point.getY(),
                    pickupLocation.getPickupStartAt(),
                    pickupLocation.getPickupEndAt(),
                    pickupLocation.getInstructions()
            );
        }
    }
}
