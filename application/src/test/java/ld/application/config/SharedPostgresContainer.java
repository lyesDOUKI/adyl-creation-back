package ld.application.config;

import org.testcontainers.containers.PostgreSQLContainer;

public final class SharedPostgresContainer {

    public static final PostgreSQLContainer<?> INSTANCE = createInstance();

    private static PostgreSQLContainer<?> createInstance() {
        try {
            PostgreSQLContainer<?> container = PostgreSQLContainer.class
                    .getConstructor(String.class)
                    .newInstance("postgres:16");

            container.withReuse(true);
            container.start();
            return container;
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'initialiser le conteneur PostgreSQL partagé", e);
        }
    }

    private SharedPostgresContainer() {}
}
