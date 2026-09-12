package ld.application.response;

import ld.application.infra.db.jooq.AppointmentQuery;
import ld.domain.features.appointment.model.AppointmentState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentsResponse(
        UUID id,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        AppointmentState status,
        OffsetDateTime cancelledAt,
        String cancelledReason
) {
    public static AppointmentsResponse from(AppointmentQuery query) {
        return new AppointmentsResponse(
                query.id(),
                query.startAt(),
                query.endAt(),
                query.status(),
                query.cancelledAt(),
                query.cancelledReason()
        );
    }
}
