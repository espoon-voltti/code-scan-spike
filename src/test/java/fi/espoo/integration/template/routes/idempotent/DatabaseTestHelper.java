package fi.espoo.integration.template.routes.idempotent;


import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class DatabaseTestHelper {
    /**
     * Hide constructor.
     */
    private DatabaseTestHelper() {
    }

    static class IdempotentDBInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:12.8");

        private static void startContainers() {
            Startables.deepStart(Stream.of(postgres)).join();
        }

        private static Map<String, Object> createConnectionConfiguration() {
            Map<String, Object> conf = new HashMap<>();

            conf.put("idempotent.database.url", postgres.getJdbcUrl());
            conf.put("idempotent.database.username", postgres.getUsername());
            conf.put("idempotent.database.password", postgres.getPassword());

            return conf;
        }

        @Override
        public void initialize(
                ConfigurableApplicationContext applicationContext) {

            startContainers();

            ConfigurableEnvironment environment =
                    applicationContext.getEnvironment();

            MapPropertySource testcontainers = new MapPropertySource(
                    "testcontainers-idempotentdb",
                    createConnectionConfiguration()
            );

            environment.getPropertySources().addFirst(testcontainers);
        }
    }

}
