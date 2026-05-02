package com.waltsoft.tx_purchase.test_container;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class ContainerTest {

    public static final String DOCKER_POSTGRES_IMAGE_NAME = "postgres:latest";

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DOCKER_POSTGRES_IMAGE_NAME).withReuse(true);

    static {
        postgres.start();
    }
}
