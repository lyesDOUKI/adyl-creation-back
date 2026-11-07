package ld.bootstrap.context;

import ld.bootstrap.bridge.OrderConfiguration;
import ld.bootstrap.config.common.TestClockConfiguration;
import ld.bootstrap.config.common.UnitOfWorkTestConfiguration;
import ld.bootstrap.config.order.DiscountProviderTestConfiguration;
import ld.bootstrap.config.order.OrderPersistenceTestConfiguration;
import ld.bootstrap.infra.db.entity.DeliveryAddressEntity;
import ld.bootstrap.infra.db.entity.OrderDetailEntity;
import ld.bootstrap.infra.db.entity.OrderEntity;
import ld.bootstrap.infra.db.entity.ProductEntity;
import ld.bootstrap.read.GetOrderServiceImpl;
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
