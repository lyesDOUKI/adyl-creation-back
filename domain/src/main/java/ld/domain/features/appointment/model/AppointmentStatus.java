package ld.domain.features.appointment.model;

import java.time.Instant;

public sealed interface AppointmentStatus {
    record Submitted(Instant submittedAt) implements AppointmentStatus{}
    record Cancelled(Instant cancelledAt, String reason) implements AppointmentStatus {}
}
