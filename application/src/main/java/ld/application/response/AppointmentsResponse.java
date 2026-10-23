package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.application.infra.db.jooq.AppointmentQuery;
import ld.domain.features.appointment.model.AppointmentState;

import java.time.OffsetDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Rendez-vous d'un client")
public record AppointmentsResponse(
        @Schema(description = "Identifiant du rendez-vous", requiredMode = REQUIRED)
        UUID id,

        @Schema(description = "Début du rendez-vous", example = "2026-09-15T09:00:00+02:00", requiredMode = REQUIRED)
        OffsetDateTime startAt,

        @Schema(description = "Fin du rendez-vous", example = "2026-09-15T09:30:00+02:00", requiredMode = REQUIRED)
        OffsetDateTime endAt,

        @Schema(description = "Statut du rendez-vous", enumAsRef = true, requiredMode = REQUIRED)
        AppointmentState status,

        @Schema(description = "Date d'annulation, présente uniquement si le rendez-vous est annulé",
                nullable = true)
        OffsetDateTime cancelledAt,

        @Schema(description = "Motif d'annulation, s'il a été renseigné", nullable = true)
        String cancelledReason
) {
    public static AppointmentsResponse from(AppointmentQuery query) {
        return new AppointmentsResponse(
                query.id(),
                query.startAt(),
                query.endAt(),
                query.status(),
                query.cancelledAt(),
                query.cancelledReason());
    }
}