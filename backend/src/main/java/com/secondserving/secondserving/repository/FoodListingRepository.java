package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodListingRepository extends JpaRepository<FoodListing, UUID> {

    List<FoodListing> findAllByOwner(User owner);

    @EntityGraph(attributePaths = {"owner", "pickupLocation"})
    Optional<FoodListing> findDetailedByListingId(UUID listingId);
}
