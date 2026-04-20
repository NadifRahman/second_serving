package com.secondserving.secondserving.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryConfig {

    /**
     * The SRID used for this backend application for all geospatial related operations. Should be consistent
     * with the database operations.
     */
    public static final int APP_SRID = 4326;

    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory(new PrecisionModel(), APP_SRID);
    }
}
