package ld.domain.features.appointment.model;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public record AppointmentSnapshot(
        UUID appointmentId,
        ZonedDateTime start,
        ZonedDateTime end,
        UUID identitySubject,
        AppointmentStatus appointmentStatus,
        Optional<AppointmentNote> note
) {}
