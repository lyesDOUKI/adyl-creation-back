package ld.application.context;

import ld.application.bridge.ProductConfiguration;
import ld.application.config.*;
import ld.application.infra.db.entity.ProductEntity;
import ld.application.infra.db.entity.ProductPhotoEntity;
import ld.application.shared.ProductTestFixture;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {
        ProductConfiguration.class,
        JooqDslConfiguration.class,
        ProductPersistenceTestConfiguration.class,
        UnitOfWorkTestConfiguration.class,
        TestClockConfiguration.class,
        ProductPhotoStorageTestConfiguration.class,
        ProductEventDispatcherTestConfiguration.class
})
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
@EntityScan(basePackageClasses = {
        ProductEntity.class,
        ProductPhotoEntity.class
})
@Testcontainers
@ActiveProfiles("test")
@Import({
        ProductTestFixture.class
})
public @interface ProductIntegrationTest {
}
