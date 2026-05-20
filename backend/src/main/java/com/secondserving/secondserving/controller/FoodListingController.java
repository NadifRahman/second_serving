package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.dto.FoodListingDto;
import com.secondserving.secondserving.service.FoodListingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping(FoodListingController.FOOD_LISTING_BASE_PATH)
public class FoodListingController {

    public static final String FOOD_LISTING_BASE_PATH = ApiPaths.API_BASE_PATH + "/food-listings";
    public static final String NEARBY_PATH = "/nearby";

    private final FoodListingService foodListingService;

    public FoodListingController(FoodListingService foodListingService) {
        this.foodListingService = foodListingService;
    }

    @GetMapping(NEARBY_PATH)
    public ResponseEntity<List<FoodListingDto>> getNearbyFoodListings(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @RequestParam @Positive double radiusMeters) {
        List<FoodListingDto> dtos = foodListingService.findNearby(longitude, latitude, radiusMeters)
                .stream()
                .map(FoodListingDto::from)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<FoodListingDto> getFoodListing(@PathVariable UUID listingId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(FoodListingDto.from(foodListingService.getFoodListingByIdOrThrow(listingId)));
    }
}
