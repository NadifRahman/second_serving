package com.secondserving.secondserving.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pickup_location")
public class PickupLocation {

    @Id
    @Column(name = "pickup_id", nullable = false)
    private UUID pickupId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false, unique = true)
    private FoodListing foodListing;

    @Column(name = "full_address", nullable = false, length = 150)
    private String fullAddress;

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "location_point", nullable = false)
    private Point locationPoint;

    @Column(name = "pickup_start_at", nullable = false)
    private Instant pickupStartAt;

    @Column(name = "pickup_end_at", nullable = false)
    private Instant pickupEndAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "instructions", length = 255)
    private String instructions;

    protected PickupLocation() {
    }

    public PickupLocation(FoodListing foodListing,
                          String fullAddress,
                          Point locationPoint,
                          Instant pickupStartAt,
                          Instant pickupEndAt,
                          String instructions) {
        this.pickupId = UUID.randomUUID();
        setFoodListing(foodListing);
        this.fullAddress = fullAddress;
        this.locationPoint = locationPoint;
        this.pickupStartAt = pickupStartAt;
        this.pickupEndAt = pickupEndAt;
        this.instructions = instructions;
    }

    public UUID getPickupId() {
        return pickupId;
    }

    public FoodListing getFoodListing() {
        return foodListing;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public Point getLocationPoint() {
        return locationPoint;
    }

    public Instant getPickupStartAt() {
        return pickupStartAt;
    }

    public Instant getPickupEndAt() {
        return pickupEndAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setFoodListing(FoodListing foodListing) {
        this.foodListing = foodListing;
        if (foodListing != null && foodListing.getPickupLocation() != this) {
            foodListing.setPickupLocation(this);
        }
    }
}
