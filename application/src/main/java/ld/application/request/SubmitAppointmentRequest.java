package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ld.domain.features.appointment.create.SubmitAppointmentCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Demande de création d'un rendez-vous")
public record SubmitAppointmentRequest(
        @NotNull
        @Schema(description = "Début du créneau souhaité", example = "2026-09-15T09:00:00+02:00")
        ZonedDateTime startAt,

        @NotNull
        @Schema(description = "Fin du créneau souhaité", example = "2026-09-15T09:30:00+02:00")
        ZonedDateTime endAt,

        @Schema(description = "Notes libres pour le rendez-vous", example = "Première visite", nullable = true)
        String notes
) {
    public SubmitAppointmentCommand to(UUID identitySubject) {
        return new SubmitAppointmentCommand(startAt, endAt, identitySubject, notes);
    }
}