package ld.application.response;

import ld.domain.features.appointment.model.TimeSlot;
import ld.spring.web.lib.ApiResponseBody;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AvailableSlotsResponse(
        List<AvailableSlotsByDateResponse> availability
) implements ApiResponseBody {

    public static AvailableSlotsResponse from(
            LocalDate from,
            LocalDate to,
            List<TimeSlot> slots
    ) {
        Map<LocalDate, List<TimeSlot>> slotsByDate = slots.stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.start().toLocalDate()
                ));

        List<AvailableSlotsByDateResponse> availability = from
                .datesUntil(to.plusDays(1))
                .map(date -> AvailableSlotsByDateResponse.from(
                        date,
                        slotsByDate.getOrDefault(date, List.of())
                ))
                .toList();

        return new AvailableSlotsResponse(availability);
    }

    public record AvailableSlotsByDateResponse(
            LocalDate date,
            List<TimeSlotResponse> slots
    ) {

        public static AvailableSlotsByDateResponse from(
                LocalDate date,
                List<TimeSlot> slots
        ) {
            return new AvailableSlotsByDateResponse(
                    date,
                    slots.stream()
                            .map(TimeSlotResponse::from)
                            .toList()
            );
        }
    }

    public record TimeSlotResponse(
            ZonedDateTime start,
            ZonedDateTime end
    ) {

        public static TimeSlotResponse from(TimeSlot timeSlot) {
            return new TimeSlotResponse(
                    timeSlot.start(),
                    timeSlot.end()
            );
        }
    }
}