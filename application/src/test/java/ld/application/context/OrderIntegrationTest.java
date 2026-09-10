package ld.application.context;

import ld.application.bridge.OrderConfiguration;
import ld.application.config.common.TestClockConfiguration;
import ld.application.config.common.UnitOfWorkTestConfiguration;
import ld.application.config.order.DiscountProviderTestConfiguration;
import ld.application.config.order.OrderPersistenceTestConfiguration;
import ld.application.infra.db.entity.*;
import ld.application.read.GetOrderServiceImpl;
import ld.application.shared.product.ProductTestFixture;
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
        OrderConfiguration.class,
        OrderPersistenceTestConfiguration.class,
        UnitOfWorkTestConfiguration.class,
        TestClockConfiguration.class,
        DiscountProviderTestConfiguration.class,
        GetOrderServiceImpl.class
})
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        JooqAutoConfiguration.class
})
@EntityScan(basePackageClasses = {
        ProductEntity.class,
        OrderEntity.class,
        OrderDetailEntity.class,
        DeliveryAddressEntity.class
})
@Testcontainers
@ActiveProfiles("test")
@Import({
        ProductTestFixture.class
})
public @interface OrderIntegrationTest {
}
