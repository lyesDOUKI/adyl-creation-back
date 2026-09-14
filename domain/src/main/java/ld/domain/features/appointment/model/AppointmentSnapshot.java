package ld.domain.features.appointment.model;

import java.util.Optional;
import java.util.UUID;

public record AppointmentSnapshot(
        UUID appointmentId,
        TimeSlot timeSlot,
        UUID identitySubject,
        AppointmentStatus appointmentStatus,
        Optional<AppointmentNote> note
) {}
