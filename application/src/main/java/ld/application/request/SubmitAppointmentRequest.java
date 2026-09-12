package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.appointment.create.SubmitAppointmentCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Demande de création d'un rendez-vous")
public record SubmitAppointmentRequest(
        @Schema(description = "Début du créneau souhaité", example = "2026-09-15T09:00:00+02:00")
        ZonedDateTime start,

        @Schema(description = "Fin du créneau souhaité", example = "2026-09-15T09:30:00+02:00")
        ZonedDateTime end,

        String notes
) {
    public SubmitAppointmentCommand to(UUID identitySubject) {
        return new SubmitAppointmentCommand(start, end, identitySubject, notes);
    }
}
