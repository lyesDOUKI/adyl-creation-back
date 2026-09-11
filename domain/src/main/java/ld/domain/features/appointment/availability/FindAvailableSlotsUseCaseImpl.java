package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.TimeSlot;
import ld.domain.features.appointment.model.TimeSlots;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class FindAvailableSlotsUseCaseImpl implements FindAvailableSlotsUseCase {

    private final OpeningHoursCalendar openingHoursCalendar;
    private final LoadBookedAppointmentsPort loadBookedAppointmentsPort;

    public FindAvailableSlotsUseCaseImpl(OpeningHoursCalendar openingHoursCalendar,
                                         LoadBookedAppointmentsPort loadBookedAppointmentsPort) {
        this.openingHoursCalendar = Objects.requireNonNull(openingHoursCalendar);
        this.loadBookedAppointmentsPort = Objects.requireNonNull(loadBookedAppointmentsPort);
    }

    @Override
    public List<TimeSlot> findAvailableSlots(FindAvailableSlotsQuery query) {
        TimeSlots theoreticalSlots =
                openingHoursCalendar.theoreticalSlotsBetween(query.from(), query.to());

        ZonedDateTime periodStart = query.from().atStartOfDay(openingHoursCalendar.zoneId());
        ZonedDateTime periodEnd = query.to().plusDays(1).atStartOfDay(openingHoursCalendar.zoneId());

        TimeSlots bookedSlots = new TimeSlots(
                loadBookedAppointmentsPort.loadBookedSlots(periodStart, periodEnd));

        return theoreticalSlots.excluding(bookedSlots).asList();
    }
}
