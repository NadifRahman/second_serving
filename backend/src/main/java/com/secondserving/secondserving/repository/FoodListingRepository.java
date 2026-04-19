package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FoodListingRepository extends JpaRepository<FoodListing, UUID> {

    List<FoodListing> findAllByOwner(User owner);
}
