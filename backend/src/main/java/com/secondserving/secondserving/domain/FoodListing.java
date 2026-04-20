package com.secondserving.secondserving.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "food_listing")
public class FoodListing {

    /** The maximum character length of a food listing's title. Should be consistent with DB constraint. */
    public static final int TITLE_MAX_LENGTH = 50;
    /** The maximum character length of a food listing's description. Should be consistent with DB constraint. */
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    /** The maximum character length of a food listing's status. Should be consistent with DB constraint. */
    public static final int STATUS_MAX_LENGTH = 30;
    /** The maximum character length of a food listing's quantity unit. Should be consistent with DB constraint. */
    public static final int QUANTITY_UNIT_MAX_LENGTH = 30;
    /** The minimum amount of quantity (of any unit) a food listing can contain. Should be consistent with DB constraint. */
    public static final int MIN_QUANTITY = 0;

    @Id
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(name = "description", length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = STATUS_MAX_LENGTH)
    private FoodListingStatus status;

    @Column(name = "quantity", nullable = false)
    @Min(MIN_QUANTITY)
    private short quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_unit", nullable = false, length = QUANTITY_UNIT_MAX_LENGTH)
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FoodListingStatus getStatus() {
        return status;
    }

    public void setStatus(FoodListingStatus status) {
        this.status = status;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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
        if (this.pickupLocation == pickupLocation) {
            // Already set before, nothing more to do
            return;
        }

        PickupLocation previousPickupLocation = this.pickupLocation;
        this.pickupLocation = pickupLocation;

        if (previousPickupLocation != null && previousPickupLocation.getFoodListing() == this) {
            // Set the previous pickup location's foodlisting to null
            previousPickupLocation.setFoodListing(null);
        }

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
