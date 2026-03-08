package com.secondserving.secondserving.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration()
@Profile("test")
public class PostgisTestContainerConfig {
    @Bean
    @ServiceConnection // Need this for spring to extract JDBC connection info from container
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:16-3.5")
                        .asCompatibleSubstituteFor("postgres")
        );
    }

}