package ld.domain.features.appointment.model;

import java.util.Objects;

public record AppointmentNote(String value) {

    private static final int MAX_LENGTH = 500;

    public AppointmentNote {
        Objects.requireNonNull(value, "value must not be null");

        value = value.trim();

        if (value.isBlank()) {
            throw new IllegalArgumentException("AppointmentNote must not be blank");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "AppointmentNote must not exceed %d characters".formatted(MAX_LENGTH));
        }
    }

    public static AppointmentNote of(String value) {
        return new AppointmentNote(value);
    }
}
