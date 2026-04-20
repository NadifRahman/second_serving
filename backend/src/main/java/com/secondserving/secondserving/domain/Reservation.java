package com.secondserving.secondserving.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reservation")
public class Reservation {


    /**
     * The minimum quantity that can be requested by any reservation. Must match database constraint.
     */
    public static final int MIN_QUANTITY_REQUESTABLE = 0;

    @EmbeddedId
    private ReservationPK reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listingId") // from composite key
    @JoinColumn(name = "listing_id", nullable = false)
    private FoodListing foodListing;

    // Don't love the fact that JPA uses stringly-typed for these composite key references....

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requesterId") // from composite key
    @JoinColumn(name = "requester_id", nullable = false)
    private User userRequester;

    @Column(name = "quantity_requested", nullable = false)
    @Min(MIN_QUANTITY_REQUESTABLE)
    private short quantityRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus reservationStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Reservation() {
    }

    public Reservation(FoodListing foodListing,
                       User userRequester,
                       short quantityRequested,
                       ReservationStatus reservationStatus) {
        this.foodListing = foodListing;
        this.userRequester = userRequester;
        this.quantityRequested = quantityRequested;
        this.reservationStatus = reservationStatus;
        this.reservationId = new ReservationPK(
                foodListing.getListingId(),
                userRequester.getUserId()
        );
    }

    public ReservationPK getReservationId() {
        return reservationId;
    }

    public FoodListing getFoodListing() {
        return foodListing;
    }

    public User getUserRequester() {
        return userRequester;
    }

    public short getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(short quantityRequested) {
        this.quantityRequested = quantityRequested;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Represents the primary key of a {@link Reservation}. This is a composite key made of two foreign keys.
     */
    @Embeddable
    public static class ReservationPK implements Serializable {
        @Column(name = "listing_id", nullable = false)
        private UUID listingId;
        @Column(name = "requester_id", nullable = false)
        private UUID requesterId;

        // No arg constructor required by JPA for embedabble key class
        private ReservationPK() {}

        public ReservationPK(UUID listingId, UUID requesterId) {
            this.listingId = listingId;
            this.requesterId = requesterId;
        }

        public UUID getListingId() {
            return listingId;
        }

        public void setListingId(UUID listingId) {
            this.listingId = listingId;
        }

        public UUID getRequesterId() {
            return requesterId;
        }

        public void setRequesterId(UUID requesterId) {
            this.requesterId = requesterId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReservationPK)) return false;
            ReservationPK that = (ReservationPK) o;
            return Objects.equals(listingId, that.listingId)
                    && Objects.equals(requesterId, that.requesterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listingId, requesterId);
        }

    }

    public enum ReservationStatus {
        REQUESTED,
        CANCELLED,
        COLLECTED
    }

}
