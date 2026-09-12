package ld.application.infra.db.jooq;

import ld.domain.features.appointment.model.AppointmentState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentQuery(
        UUID id,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        UUID identitySubject,
        AppointmentState status,
        OffsetDateTime submittedAt,
        OffsetDateTime cancelledAt,
        String cancelledReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
