package com.secondserving.secondserving.service;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.PickupLocation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.exception.FoodListingNotFoundException;
import com.secondserving.secondserving.exception.UpdatingUnownedFoodListingException;
import com.secondserving.secondserving.repository.FoodListingRepository;
import com.secondserving.secondserving.repository.PickupLocationRepository;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
     * @param owner the (proposed) owner of the food listing
     * @param listingId The id of the listing to update.
     * @param command The updated listing field values.
     * @return The saved {@link FoodListing}
     * @throws FoodListingNotFoundException If the listing cannot be found
     * @throws UpdatingUnownedFoodListingException If the passed in user does not own the listingId
     */
    public FoodListing patchListingIfOwnedByUserOrThrow(User owner, UUID listingId, PatchFoodListingCommand command) {

        FoodListing listing = getFoodListingByIdOrThrow(listingId);

        if (!listing.isOwnedBy(owner)) {
            throw new UpdatingUnownedFoodListingException("You cannot update a food listing owned by another user.");
        }

        // Patch fields if they are not null
        if (command.status() != null) {
            listing.setStatus(command.status());
        }
        if (command.quantity() != null) {
            listing.setQuantity(command.quantity());
        }
        if (command.expiresAt() != null) {
            listing.setExpiresAt(command.expiresAt());
        }
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
        FoodListing listing = getFoodListingByIdOrThrow(listingId);
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
     * Get the food listing JPA object given the ID or throw {@link FoodListingNotFoundException}
     * @param listingId the ID of the food listing
     * @throws FoodListingNotFoundException if food listing not found
     * @return the food listing
     */
    public FoodListing getFoodListingByIdOrThrow(UUID listingId) {
        return foodListingRepository.findDetailedByListingId(listingId)
                .orElseThrow(() -> new FoodListingNotFoundException("Could not find food listing with id " + listingId));
    }

    /**
     * Service method to delete a {@link FoodListing} only if the passed in {@link User} owns the listing.
     *
     * Note that the cascading of deletes that depend on this {@link FoodListing} is assumed to be done
     * at the database level. 
     * @param user The authenticated user
     * @param listingId
     */
    public void deleteFoodListingIfOwnedOrThrow(User user, UUID listingId) {
        try {
            FoodListing foodListing = getFoodListingByIdOrThrow(listingId);
            if (!foodListing.isOwnedBy(user)) {
                throw new UpdatingUnownedFoodListingException("You cannot delete a food listing owned by another user.");
            } else {
                foodListingRepository.delete(foodListing);
            }
        } catch (FoodListingNotFoundException e) {
            // In case client tries sending multiple request to delete, send a useful failure message that we may have already done it
            throw new FoodListingNotFoundException("Could not find food listing with id " + listingId + ". It may have been deleted already.");
        }
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
     * Input data for patching the mutable fields of an existing {@link FoodListing}.
     * Fields should be set to null if they should not be applied to the patch
     */
    public record PatchFoodListingCommand(FoodListing.FoodListingStatus status,
                                          Short quantity,
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
