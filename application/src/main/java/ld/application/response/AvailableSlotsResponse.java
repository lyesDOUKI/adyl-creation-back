package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.appointment.model.TimeSlot;
import ld.spring.web.lib.ApiResponseBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Créneaux disponibles, regroupés par date")
public record AvailableSlotsResponse(
        @Schema(description = "Une entrée par date de la période demandée, y compris celles sans créneau",
                requiredMode = REQUIRED)
        List<AvailableSlotsByDateResponse> availability
) implements ApiResponseBody {

    public static AvailableSlotsResponse from(LocalDate from, LocalDate to, List<TimeSlot> slots) {
        Map<LocalDate, List<TimeSlot>> slotsByDate = slots.stream()
                .collect(Collectors.groupingBy(slot -> slot.start().toLocalDate()));

        List<AvailableSlotsByDateResponse> availability = from
                .datesUntil(to.plusDays(1))
                .map(date -> AvailableSlotsByDateResponse.from(date, slotsByDate.getOrDefault(date, List.of())))
                .toList();

        return new AvailableSlotsResponse(availability);
    }

    @Schema(description = "Créneaux disponibles pour une date")
    public record AvailableSlotsByDateResponse(
            @Schema(description = "Date concernée", example = "2026-09-15", requiredMode = REQUIRED)
            LocalDate date,

            @Schema(description = "Créneaux disponibles ce jour-là (liste vide s'il n'y en a aucun)",
                    requiredMode = REQUIRED)
            List<TimeSlotResponse> slots
    ) {
        public static AvailableSlotsByDateResponse from(LocalDate date, List<TimeSlot> slots) {
            return new AvailableSlotsByDateResponse(
                    date,
                    slots.stream().map(TimeSlotResponse::from).toList());
        }
    }
}