package ld.application.bridge;

import ld.domain.features.appointment.availability.*;
import ld.domain.features.appointment.create.AppointmentCreator;
import ld.domain.features.appointment.create.ScheduleClaimer;
import ld.domain.features.appointment.create.SubmitAppointmentUseCase;
import ld.domain.features.appointment.create.SubmitAppointmentUseCaseImpl;
import ld.domain.features.appointment.model.AppointmentEvent;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppointmentConfiguration {


    @Bean
    public OpeningHoursCalendar openingHoursCalendar() {
        return new DefaultOpeningHoursCalendar();
    }

    @Bean
    public FindAvailableSlotsUseCase findAvailableSlotsUseCase(OpeningHoursCalendar openingHoursCalendar,
                                                               LoadBookedAppointmentsPort loadBookedAppointmentsPort) {
        return new FindAvailableSlotsUseCaseImpl(openingHoursCalendar, loadBookedAppointmentsPort);
    }

    @Bean
    public AggregateEventDispatcher<AppointmentEvent> appointmentEventAggregateEventDispatcher() {
        return new InMemoryAggregateEventDispatcher<>();
    }
    @Bean
    public SubmitAppointmentUseCase submitAppointmentUseCase(AppointmentCreator appointmentCreator,
                                                             ScheduleClaimer scheduleClaimer,
                                                             OpeningHoursCalendar openingHoursCalendar,
                                                             AggregateEventDispatcher<AppointmentEvent> appointmentEventAggregateEventDispatcher,
                                                             UnitOfWork unitOfWork,
                                                             Clock clock
                                                             ) {
        return new SubmitAppointmentUseCaseImpl(appointmentCreator,
                scheduleClaimer,
                openingHoursCalendar,
                appointmentEventAggregateEventDispatcher,
                unitOfWork,
                clock);
    }
}
