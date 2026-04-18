package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.domain.FoodListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodListingRepository extends JpaRepository<FoodListing, UUID> {
}
