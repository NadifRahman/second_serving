package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.PickupLocation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.repository.FoodListingRepository;
import com.secondserving.secondserving.repository.PickupLocationRepository;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FoodListingService {

    private final FoodListingRepository foodListingRepository;
    private final PickupLocationRepository pickupLocationRepository;

    public FoodListingService(FoodListingRepository foodListingRepository,
                              PickupLocationRepository pickupLocationRepository) {
        this.foodListingRepository = foodListingRepository;
        this.pickupLocationRepository = pickupLocationRepository;
    }

    /**
     * Creates a new food listing for the given owner and its required pickup location.
     *
     * @param owner The authenticated owner creating the listing
     * @param command The data required to create the listing aggregate
     * @return The saved {@link FoodListing}
     * @throws IllegalArgumentException If pickup location data is missing
     */
    public FoodListing createListing(User owner, CreateFoodListingCommand command) {
        if (command.pickupLocation() == null) {
            throw new IllegalArgumentException("Pickup location is required");
        }

        FoodListing listing = new FoodListing(
                owner,
                command.title(),
                command.description(),
                command.status(),
                command.quantity(),
                command.quantityUnit(),
                command.expiresAt()
        );
        // Constructor wires the connection
        new PickupLocation(
                listing,
                command.pickupLocation().fullAddress(),
                command.pickupLocation().locationPoint(),
                command.pickupLocation().pickupStartAt(),
                command.pickupLocation().pickupEndAt(),
                command.pickupLocation().instructions()
        );

        return foodListingRepository.save(listing);
    }

    /**
     * Updates the mutable listing fields for an existing food listing.
     *
     * @param listingId The id of the listing to update
     * @param command The updated listing field values
     * @return The saved {@link FoodListing}
     * @throws IllegalArgumentException If the listing cannot be found
     */
    public FoodListing updateListing(UUID listingId, UpdateFoodListingCommand command) {
        FoodListing listing = getListingByIdOrThrow(listingId);

        listing.setTitle(command.title());
        listing.setDescription(command.description());
        listing.setStatus(command.status());
        listing.setQuantity(command.quantity());
        listing.setQuantityUnit(command.quantityUnit());
        listing.setExpiresAt(command.expiresAt());

        return foodListingRepository.save(listing);
    }

    /**
     * Creates or updates the pickup location associated with a food listing.
     *
     * @param listingId The id of the listing whose pickup location should be updated
     * @param command The pickup location data to apply
     * @return The saved {@link FoodListing}
     * @throws IllegalArgumentException If the listing cannot be found
     */
    public FoodListing updatePickupLocation(UUID listingId, PickupLocationCommand command) {
        FoodListing listing = getListingByIdOrThrow(listingId);
        PickupLocation pickupLocation = listing.getPickupLocation();

        if (pickupLocation == null) {
            new PickupLocation(
                    listing,
                    command.fullAddress(),
                    command.locationPoint(),
                    command.pickupStartAt(),
                    command.pickupEndAt(),
                    command.instructions()
            );
        } else {
            pickupLocation.setFullAddress(command.fullAddress());
            pickupLocation.setLocationPoint(command.locationPoint());
            pickupLocation.setPickupStartAt(command.pickupStartAt());
            pickupLocation.setPickupEndAt(command.pickupEndAt());
            pickupLocation.setInstructions(command.instructions());
        }

        return foodListingRepository.save(listing);
    }

    /**
     * Finds food listings whose pickup locations are within a radius of the given coordinate.
     *
     * @param longitude The longitude of the center point
     * @param latitude The latitude of the center point
     * @param radiusMeters The search radius in meters
     * @return All nearby {@link FoodListing} entities
     */
    public List<FoodListing> findNearby(double longitude, double latitude, double radiusMeters) {
        return pickupLocationRepository.findNearby(longitude, latitude, radiusMeters)
                .stream()
                .map(PickupLocation::getFoodListing)
                .toList();
    }

    /**
     * Fetches all food listings owned by a given user.
     *
     * @param owner The owner
     * @return The owner's food listings
     */
    public List<FoodListing> getFoodListingsOwnedBy(User owner) {
        return foodListingRepository.findAllByOwner(owner);
    }

    /**
     * Finds a food listing by its id.
     *
     * @param listingId The id of the listing
     * @return An {@link Optional} containing the listing when found
     */
    public Optional<FoodListing> findById(UUID listingId) {
        return foodListingRepository.findById(listingId);
    }

    private FoodListing getListingByIdOrThrow(UUID listingId) {
        return foodListingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find food listing with id " + listingId));
    }

    /**
     * Input data for creating a new {@link FoodListing}, including its required pickup details.
     */
    public record CreateFoodListingCommand(String title,
                                           String description,
                                           FoodListing.FoodListingStatus status,
                                           short quantity,
                                           FoodListing.QuantityUnit quantityUnit,
                                           Instant expiresAt,
                                           PickupLocationCommand pickupLocation) {
    }

    /**
     * Input data for updating the mutable fields of an existing {@link FoodListing}.
     */
    public record UpdateFoodListingCommand(String title,
                                           String description,
                                           FoodListing.FoodListingStatus status,
                                           short quantity,
                                           FoodListing.QuantityUnit quantityUnit,
                                           Instant expiresAt) {
    }

    /**
     * Input data for creating or updating a {@link PickupLocation}.
     */
    public record PickupLocationCommand(String fullAddress,
                                        Point locationPoint,
                                        Instant pickupStartAt,
                                        Instant pickupEndAt,
                                        String instructions) {
    }
}
