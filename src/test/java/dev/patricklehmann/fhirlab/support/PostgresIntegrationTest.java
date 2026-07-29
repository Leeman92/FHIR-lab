package dev.patricklehmann.fhirlab.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for tests that need the real schema.
 *
 * <p>The container follows the singleton pattern — started once in a static initializer and never
 * stopped explicitly — so it is shared by every subclass and does not invalidate Spring's cached
 * application context between test classes. Ryuk removes it when the JVM exits. Flyway runs the
 * production migrations against it on context startup, so constraints and Postgres-specific
 * functions are exercised for real rather than against an in-memory substitute.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class PostgresIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.4");

    static {
        POSTGRES.start();
    }
}
