package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.domain.features.appointment.model.AppointmentState;
import ld.domain.features.appointment.model.AppointmentStatus;
import ld.spring.web.lib.ApiResponseBody;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Rendez-vous créé")
public record SubmitAppointmentResponse(
        @Schema(description = "Identifiant du rendez-vous créé", requiredMode = REQUIRED)
        UUID appointmentId,

        @Schema(description = "Début du créneau réservé", example = "2026-09-15T09:00:00+02:00", requiredMode = REQUIRED)
        ZonedDateTime startAt,

        @Schema(description = "Fin du créneau réservé", example = "2026-09-15T09:30:00+02:00", requiredMode = REQUIRED)
        ZonedDateTime endAt,

        @Schema(description = "Statut du rendez-vous (toujours SUBMITTED à la création)",
                enumAsRef = true, requiredMode = REQUIRED)
        AppointmentState status,

        @Schema(description = "Horodatage de la soumission", example = "2026-09-15T07:00:00Z", requiredMode = REQUIRED)
        Instant submittedAt
) implements ApiResponseBody {

    public static SubmitAppointmentResponse from(AppointmentSnapshot appointmentSnapshot) {
        if (!(appointmentSnapshot.appointmentStatus() instanceof AppointmentStatus.Submitted(Instant submittedAt))) {
            throw new IllegalArgumentException("Appointment snapshot must have a SUBMITTED status");
        }
        return new SubmitAppointmentResponse(
                appointmentSnapshot.appointmentId(),
                appointmentSnapshot.timeSlot().start(),
                appointmentSnapshot.timeSlot().end(),
                AppointmentState.SUBMITTED,
                submittedAt);
    }
}