package ld.domain.features.appointment.create;

import ld.domain.features.appointment.availability.OpeningHoursCalendar;
import ld.domain.features.appointment.model.TimeSlot;
import ld.domain.features.appointment.model.TimeSlots;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class SlotWithinOpeningHoursRule implements BusinessRule<SubmitAppointmentCommand> {

    private final OpeningHoursCalendar openingHoursCalendar;

    public SlotWithinOpeningHoursRule(OpeningHoursCalendar openingHoursCalendar) {
        this.openingHoursCalendar = Objects.requireNonNull(openingHoursCalendar);
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

        return Result.ok();
    }
}