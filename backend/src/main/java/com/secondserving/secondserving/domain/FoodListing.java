package com.secondserving.secondserving.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "food_listing")
public class FoodListing {

    @Id
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FoodListingStatus status;

    // min size is not guarded here
    @Column(name = "quantity", nullable = false)
    private short quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_unit", nullable = false, length = 30)
    private QuantityUnit quantityUnit;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Hibernate cannot guarantee lazy loading since this side does not own the relationship
    @OneToOne(mappedBy = "foodListing", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private PickupLocation pickupLocation;

    protected FoodListing() {
    }

    public FoodListing(User owner,
                       String title,
                       String description,
                       FoodListingStatus status,
                       short quantity,
                       QuantityUnit quantityUnit,
                       Instant expiresAt) {
        this.listingId = UUID.randomUUID();
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.status = status;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
        this.expiresAt = expiresAt;
    }

    public UUID getListingId() {
        return listingId;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public FoodListingStatus getStatus() {
        return status;
    }

    public short getQuantity() {
        return quantity;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public PickupLocation getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(PickupLocation pickupLocation) {
        this.pickupLocation = pickupLocation;
        if (pickupLocation != null && pickupLocation.getFoodListing() != this) {
            pickupLocation.setFoodListing(this);
        }
    }

    public enum FoodListingStatus {
        AVAILABLE,
        FINISHED,
        CANCELLED
    }

    public enum QuantityUnit {
        ITEM,
        PORTION,
        SERVING,
        GRAM,
        KILOGRAM,
        MILLILITER,
        LITER,
        DOZEN,
        PACKAGE
    }
}
