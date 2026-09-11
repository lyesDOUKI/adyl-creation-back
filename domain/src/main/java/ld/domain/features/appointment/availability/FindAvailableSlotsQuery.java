package ld.domain.features.appointment.availability;

import java.time.LocalDate;
import java.util.Objects;

public record FindAvailableSlotsQuery(LocalDate from, LocalDate to) {

    public FindAvailableSlotsQuery {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' must not be before 'from'");
        }
    }
}