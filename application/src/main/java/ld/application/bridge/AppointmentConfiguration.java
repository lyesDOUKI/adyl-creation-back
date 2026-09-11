package ld.application.bridge;

import ld.domain.features.appointment.availability.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
