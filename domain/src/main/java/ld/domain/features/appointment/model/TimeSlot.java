package ld.domain.features.appointment.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public record TimeSlot(ZonedDateTime start, ZonedDateTime end) {

    public TimeSlot {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be strictly after start");
        }
    }

    public boolean overlaps(TimeSlot other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }
}

