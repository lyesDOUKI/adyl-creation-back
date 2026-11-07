package ld.bootstrap.context;

import ld.bootstrap.bridge.AppointmentConfiguration;
import ld.bootstrap.config.appointment.AppointmentPersistenceTestConfiguration;
import ld.bootstrap.config.common.TestClockConfiguration;
import ld.bootstrap.config.common.UnitOfWorkTestConfiguration;
import ld.bootstrap.infra.db.entity.AppointmentEntity;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {
        AppointmentConfiguration.class,
        AppointmentPersistenceTestConfiguration.class,
        UnitOfWorkTestConfiguration.class,
        TestClockConfiguration.class
})
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        JooqAutoConfiguration.class
})
@EntityScan(basePackageClasses = {
        AppointmentEntity.class
})
@Testcontainers
@ActiveProfiles("test")
public @interface AppointmentIntegrationTest {
}
