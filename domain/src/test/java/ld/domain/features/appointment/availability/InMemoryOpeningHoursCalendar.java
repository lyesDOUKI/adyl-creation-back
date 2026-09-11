package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.OpeningInterval;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryOpeningHoursCalendar implements OpeningHoursCalendar {

    private final Map<LocalDate, List<OpeningInterval>> openingIntervalsByDate = new HashMap<>();
    private Duration slotDuration = Duration.ofMinutes(30);
    private ZoneId zoneId = ZoneId.of("Europe/Paris");

    public InMemoryOpeningHoursCalendar withOpeningIntervals(LocalDate date, OpeningInterval... intervals) {
        openingIntervalsByDate.put(date, List.of(intervals));
        return this;
    }

    InMemoryOpeningHoursCalendar withSlotDuration(Duration slotDuration) {
        this.slotDuration = slotDuration;
        return this;
    }

    InMemoryOpeningHoursCalendar withZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    @Override
    public List<OpeningInterval> openingIntervalsFor(LocalDate date) {
        return openingIntervalsByDate.getOrDefault(date, List.of());
    }

    @Override
    public Duration slotDuration() {
        return slotDuration;
    }

    @Override
    public ZoneId zoneId() {
        return zoneId;
    }
}