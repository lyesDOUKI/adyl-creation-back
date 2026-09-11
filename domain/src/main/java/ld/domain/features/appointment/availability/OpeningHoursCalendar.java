package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.OpeningInterval;
import ld.domain.features.appointment.model.TimeSlot;
import ld.domain.features.appointment.model.TimeSlots;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface OpeningHoursCalendar {

    List<OpeningInterval> openingIntervalsFor(LocalDate date);

    Duration slotDuration();

    ZoneId zoneId();

    default TimeSlots theoreticalSlotsBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' must not be before 'from'");
        }

        List<TimeSlot> slots = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            for (OpeningInterval interval : openingIntervalsFor(date)) {
                slots.addAll(slotsWithinInterval(date, interval));
            }
        }
        return new TimeSlots(slots);
    }

    private List<TimeSlot> slotsWithinInterval(LocalDate date, OpeningInterval interval) {
        List<TimeSlot> slots = new ArrayList<>();
        ZonedDateTime cursor = ZonedDateTime.of(date, interval.start(), zoneId());
        ZonedDateTime intervalEnd = ZonedDateTime.of(date, interval.end(), zoneId());

        while (true) {
            ZonedDateTime slotEnd = cursor.plus(slotDuration());
            if (slotEnd.isAfter(intervalEnd)) {
                break;
            }
            slots.add(new TimeSlot(cursor, slotEnd));
            cursor = slotEnd;
        }
        return slots;
    }
}
