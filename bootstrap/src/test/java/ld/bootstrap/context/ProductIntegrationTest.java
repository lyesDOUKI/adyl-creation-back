package ld.bootstrap.context;

import ld.bootstrap.bridge.ProductConfiguration;
import ld.bootstrap.config.common.TestClockConfiguration;
import ld.bootstrap.config.common.UnitOfWorkTestConfiguration;
import ld.bootstrap.config.product.ProductPersistenceTestConfiguration;
import ld.bootstrap.config.product.ProductPhotoStorageTestConfiguration;
import ld.bootstrap.infra.db.entity.ProductEntity;
import ld.bootstrap.infra.db.entity.ProductPhotoEntity;
import ld.bootstrap.read.GetProductServiceImpl;
import ld.bootstrap.shared.product.ProductTestFixture;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
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
        ProductPersistenceTestConfiguration.class,
        UnitOfWorkTestConfiguration.class,
        TestClockConfiguration.class,
        GetProductServiceImpl.class,
        ProductPhotoStorageTestConfiguration.class
})
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        JooqAutoConfiguration.class
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
