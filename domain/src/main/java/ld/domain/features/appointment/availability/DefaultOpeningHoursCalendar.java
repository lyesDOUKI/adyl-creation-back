package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.OpeningInterval;

import java.time.*;
import java.util.*;

public class DefaultOpeningHoursCalendar implements OpeningHoursCalendar {
    private final Map<DayOfWeek, List<OpeningInterval>> weeklyOpeningHours;
    private final Duration slotDuration;
    private final Set<LocalDate> closedDates;
    private final ZoneId zoneId;

    public DefaultOpeningHoursCalendar() {
        this(defaultWeeklyOpeningHours(), Duration.ofMinutes(30), Set.of(), ZoneId.of("Europe/Paris"));
    }

    public DefaultOpeningHoursCalendar(Map<DayOfWeek, List<OpeningInterval>> weeklyOpeningHours,
                                       Duration slotDuration,
                                       Set<LocalDate> closedDates,
                                       ZoneId zoneId) {
        Objects.requireNonNull(weeklyOpeningHours, "weeklyOpeningHours must not be null");
        Objects.requireNonNull(slotDuration, "slotDuration must not be null");
        Objects.requireNonNull(closedDates, "closedDates must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        if (slotDuration.isZero() || slotDuration.isNegative()) {
            throw new IllegalArgumentException("slotDuration must be strictly positive");
        }

        this.weeklyOpeningHours = Collections.unmodifiableMap(new EnumMap<>(weeklyOpeningHours));
        this.slotDuration = slotDuration;
        this.closedDates = Set.copyOf(closedDates);
        this.zoneId = zoneId;
    }

    private static Map<DayOfWeek, List<OpeningInterval>> defaultWeeklyOpeningHours() {
        Map<DayOfWeek, List<OpeningInterval>> hours = new EnumMap<>(DayOfWeek.class);
        List<OpeningInterval> weekdayHours = List.of(
                new OpeningInterval(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new OpeningInterval(LocalTime.of(14, 0), LocalTime.of(18, 0))
        );
        hours.put(DayOfWeek.MONDAY, weekdayHours);
        hours.put(DayOfWeek.TUESDAY, weekdayHours);
        hours.put(DayOfWeek.WEDNESDAY, weekdayHours);
        hours.put(DayOfWeek.THURSDAY, weekdayHours);
        hours.put(DayOfWeek.FRIDAY, weekdayHours);
        return hours;
    }

    @Override
    public List<OpeningInterval> openingIntervalsFor(LocalDate date) {
        if (closedDates.contains(date)) {
            return List.of();
        }
        return weeklyOpeningHours.getOrDefault(date.getDayOfWeek(), List.of());
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
