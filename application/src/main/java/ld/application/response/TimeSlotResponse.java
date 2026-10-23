package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.appointment.model.TimeSlot;

import java.time.ZonedDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Créneau horaire")
public record TimeSlotResponse(
        @Schema(description = "Début du créneau", example = "2026-09-15T09:00:00+02:00", requiredMode = REQUIRED)
        ZonedDateTime start,

        @Schema(description = "Fin du créneau", example = "2026-09-15T09:30:00+02:00", requiredMode = REQUIRED)
        ZonedDateTime end
) {
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(timeSlot.start(), timeSlot.end());
    }
}