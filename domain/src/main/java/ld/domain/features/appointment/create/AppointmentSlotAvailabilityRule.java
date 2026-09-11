package ld.domain.features.appointment.create;

import ld.domain.features.appointment.availability.LoadBookedAppointmentsPort;
import ld.domain.features.appointment.availability.OpeningHoursCalendar;
import ld.domain.features.appointment.model.TimeSlot;
import ld.domain.features.appointment.model.TimeSlots;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public class AppointmentSlotAvailabilityRule implements BusinessRule<SubmitAppointmentCommand> {

    private final OpeningHoursCalendar openingHoursCalendar;
    private final LoadBookedAppointmentsPort loadBookedAppointmentsPort;

    public AppointmentSlotAvailabilityRule(OpeningHoursCalendar openingHoursCalendar,
                                           LoadBookedAppointmentsPort loadBookedAppointmentsPort) {
        this.openingHoursCalendar = Objects.requireNonNull(openingHoursCalendar);
        this.loadBookedAppointmentsPort = Objects.requireNonNull(loadBookedAppointmentsPort);
    }

    @Override
    public Result<Void> apply(SubmitAppointmentCommand command) {
        ZoneId businessZone = openingHoursCalendar.zoneId();
        TimeSlot requestedSlot = new TimeSlot(
                command.start().withZoneSameInstant(businessZone),
                command.end().withZoneSameInstant(businessZone)
        );

        LocalDate date = requestedSlot.start().toLocalDate();

        TimeSlots theoreticalSlots = openingHoursCalendar.theoreticalSlotsBetween(date, date);
        if (!theoreticalSlots.asList().contains(requestedSlot)) {
            return Result.businessFailure(
                    AppointmentErrorCode.SLOT_OUTSIDE_OPENING_HOURS,
                    "Créneau hors horaires",
                    "Le créneau demandé est hors horaire autorisé"
            );
        }

        ZonedDateTime dayStart = date.atStartOfDay(businessZone);
        ZonedDateTime dayEnd = date.plusDays(1).atStartOfDay(businessZone);
        TimeSlots bookedSlots = new TimeSlots(loadBookedAppointmentsPort.loadBookedSlots(dayStart, dayEnd));

        if (bookedSlots.asList().stream().anyMatch(requestedSlot::overlaps)) {
            return Result.businessFailure(
                    AppointmentErrorCode.SLOT_ALREADY_BOOKED,
                    "Créneau déjà réservé",
                    "Ce créneau est déjà réservé"
            );
        }

        return Result.ok();
    }
}
