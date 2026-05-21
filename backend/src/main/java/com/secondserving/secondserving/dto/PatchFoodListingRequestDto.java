package com.secondserving.secondserving.dto;

import com.secondserving.secondserving.domain.FoodListing;
import jakarta.validation.constraints.*;

import java.time.Instant;

/**
 * DTO that holds data for patching a food listing with allowed fields to change. Fields can be null, suggesting
 * requester does not want them set
 * @param status
 * @param quantity
 * @param expiresAt
 */
public record PatchFoodListingRequestDto(
        FoodListing.FoodListingStatus status,

        @Min(FoodListing.MIN_QUANTITY)
        Short quantity,

        Instant expiresAt
) {
}
