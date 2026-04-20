package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.PickupLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * The DTO representing data needed to create a food listing, assuming we already have the user who owns this food listing.
 * @param title
 * @param description
 * @param status
 * @param quantity
 * @param quantityUnit
 * @param expiresAt
 * @param pickupLocation
 */
public record CreateFoodListingRequestDto(
        @NotBlank
        @Size(max = FoodListing.TITLE_MAX_LENGTH)
        String title,

        @Size(max = FoodListing.DESCRIPTION_MAX_LENGTH)
        String description,

        @NotNull
        FoodListing.FoodListingStatus status,

        @NotNull
        @Min(FoodListing.MIN_QUANTITY)
        Short quantity,

        @NotNull
        FoodListing.QuantityUnit quantityUnit,

        @NotNull
        @Future
        Instant expiresAt,

        @NotNull
        @Valid
        PickupLocationRequestDto pickupLocation
) {
    public record PickupLocationRequestDto(
            @NotBlank
            @Size(max = PickupLocation.FULL_ADDRESS_MAX_LENGTH)
            String fullAddress,

            @NotNull
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            Double longitude,

            @NotNull
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            Double latitude,

            @NotNull
            Instant pickupStartAt,

            @NotNull
            Instant pickupEndAt,

            @Size(max = PickupLocation.INSTRUCTIONS_MAX_LENGTH)
            String instructions
    ) {
    }
}
