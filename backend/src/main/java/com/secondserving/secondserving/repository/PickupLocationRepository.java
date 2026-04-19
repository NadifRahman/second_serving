package com.secondserving.secondserving.repository;

import com.secondserving.secondserving.domain.PickupLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PickupLocationRepository extends JpaRepository<PickupLocation, UUID> {

    /**
     * Finds the pick up locations within the radius distance of the given point
     * @param longitude The longitude of the point
     * @param latitude The latitude of the point
     * @param radiusMeters The distance of the radius to find pickups around
     * @return
     */
    @Query(value = """
            SELECT p.*
            FROM pickup_location p
            WHERE ST_DWithin(
                p.location_point,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                :radiusMeters
            )
            """, nativeQuery = true)
    List<PickupLocation> findNearby(@Param("longitude") double longitude,
                                    @Param("latitude") double latitude,
                                    @Param("radiusMeters") double radiusMeters);
}
