package ld.application.api.resolver;

import ld.application.response.AvailableSlotsResponse;
import ld.domain.features.appointment.model.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvailableSlotsResponseMapper {

    private AvailableSlotsResponseMapper() {}
    public static AvailableSlotsResponse toResponse(
            LocalDate from,
            LocalDate to,
            List<TimeSlot> slots
    ) {
        Map<LocalDate, List<TimeSlot>> slotsByDate = slots.stream()
                .collect(Collectors.groupingBy(slot -> slot.start().toLocalDate()));

        List<AvailableSlotsResponse.AvailableSlotsByDateResponse> availability = from
                .datesUntil(to.plusDays(1))
                .map(date -> toDailyResponse(date, slotsByDate.getOrDefault(date, List.of())))
                .toList();

        return new AvailableSlotsResponse(availability);
    }

    private static AvailableSlotsResponse.AvailableSlotsByDateResponse toDailyResponse(
            LocalDate date,
            List<TimeSlot> slots
    ) {
        return new AvailableSlotsResponse.AvailableSlotsByDateResponse(
                date,
                slots.stream().map(AvailableSlotsResponseMapper::toSlotResponse).toList()
        );
    }

    private static AvailableSlotsResponse.TimeSlotResponse toSlotResponse(TimeSlot slot) {
        return new AvailableSlotsResponse.TimeSlotResponse(slot.start(), slot.end());
    }
}
