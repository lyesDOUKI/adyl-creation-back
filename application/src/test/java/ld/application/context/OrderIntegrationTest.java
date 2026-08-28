package ld.application.context;

import ld.application.bridge.OrderConfiguration;
import ld.application.config.JooqDslConfiguration;
import ld.application.config.OrderPersistenceTestConfiguration;
import ld.application.config.UnitOfWorkTestConfiguration;
import ld.application.infra.db.entity.Customer;
import ld.application.infra.db.entity.Order;
import ld.application.infra.db.entity.OrderDetail;
import ld.application.infra.db.entity.Product;
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
        OrderConfiguration.class,
        JooqDslConfiguration.class,
        OrderPersistenceTestConfiguration.class,
        UnitOfWorkTestConfiguration.class
})
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
@EntityScan(basePackageClasses = {
        Product.class,
        Order.class,
        OrderDetail.class,
        Customer.class
})
@Testcontainers
@ActiveProfiles("test")
@Import({
        ProductTestFixture.class
})
public @interface OrderIntegrationTest {
}
