package ld.domain.features.appointment.model;

import java.time.LocalTime;
import java.util.Objects;

public record OpeningInterval(LocalTime start, LocalTime end) {

    public OpeningInterval {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }
}

