package com.openshelf;

import com.openshelf.catalogue.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-context integration test against a real PostgreSQL instance.
 *
 * <p>Requires Docker. When Docker is unavailable the test is skipped rather than failed,
 * so the unit/service suite still runs in restricted build environments.</p>
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("openshelf")
                    .withUsername("openshelf")
                    .withPassword("openshelf");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.bootstrap-admin.enabled", () -> "false");
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-at-least-32-bytes");
    }

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void flywayMigrationsSeedTheCatalogue() {
        assertThat(resourceRepository.count()).isEqualTo(30);
        assertThat(resourceRepository.countActive()).isEqualTo(30);
    }
}
