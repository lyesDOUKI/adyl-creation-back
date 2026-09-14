package ld.application.config.appointment;

import ld.application.infra.db.jooq.JooqLoadBookedAppointmentsAdapter;
import ld.application.infra.db.jooq.ScheduleClaimerJooqAdapter;
import ld.application.infra.db.jpa.AppointmentJpaRepository;
import ld.application.infra.db.jpa.ProductJpaRepository;
import ld.application.infra.db.jpa.adapter.AppointmentCreatorJpaAdapter;
import ld.domain.features.appointment.availability.LoadBookedAppointmentsPort;
import ld.domain.features.appointment.create.ScheduleClaimer;
import org.jooq.DSLContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackageClasses = { ProductJpaRepository.class })
public class AppointmentPersistenceTestConfiguration {

    @Bean
    LoadBookedAppointmentsPort loadBookedAppointmentsPort(DSLContext dslContext) {
        return new JooqLoadBookedAppointmentsAdapter(dslContext);
    }

    @Bean
    AppointmentCreatorJpaAdapter appointmentCreatorJpaAdapter(
            AppointmentJpaRepository appointmentJpaRepository) {
        return new AppointmentCreatorJpaAdapter(appointmentJpaRepository);
    }

    @Bean
    @Primary
    SwitchableAppointmentCreator switchableAppointmentCreator(
            AppointmentCreatorJpaAdapter delegate) {
        return new SwitchableAppointmentCreator(delegate);
    }

    @Bean
    ScheduleClaimer scheduleClaimer(DSLContext dslContext) {
        return new ScheduleClaimerJooqAdapter(dslContext);
    }
}