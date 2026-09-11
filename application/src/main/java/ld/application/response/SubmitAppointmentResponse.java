package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.domain.features.appointment.model.AppointmentState;
import ld.domain.features.appointment.model.AppointmentStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Rendez-vous créé")
public record SubmitAppointmentResponse(
        @Schema(description = "Identifiant du rendez-vous créé")
        UUID appointmentId,

        @Schema(description = "Début du créneau réservé", example = "2026-09-15T09:00:00+02:00")
        ZonedDateTime start,

        @Schema(description = "Fin du créneau réservé", example = "2026-09-15T09:30:00+02:00")
        ZonedDateTime end,

        @Schema(description = "Statut du rendez-vous", example = "SUBMITTED")
        String status,

        @Schema(description = "Horodatage de la soumission", example = "2026-09-15T07:00:00Z")
        Instant submittedAt
) implements ApiResponseBody {

    public static SubmitAppointmentResponse from(AppointmentSnapshot appointmentSnapshot) {
        if (!(appointmentSnapshot.appointmentStatus() instanceof AppointmentStatus.Submitted(Instant submittedAt))) {
            throw new IllegalArgumentException(
                    "Appointment snapshot must have a SUBMITTED status"
            );
        }
        return new SubmitAppointmentResponse(
                appointmentSnapshot.appointmentId(),
                appointmentSnapshot.start(),
                appointmentSnapshot.end(),
                AppointmentState.SUBMITTED.name(),
                submittedAt
        );
    }
}