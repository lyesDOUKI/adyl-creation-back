package ld.application.read;

import ld.application.api.resolver.AvailableSlotsResponseMapper;
import ld.application.response.AvailableSlotsResponse;
import ld.domain.features.appointment.availability.FindAvailableSlotsQuery;
import ld.domain.features.appointment.availability.FindAvailableSlotsUseCase;
import ld.domain.features.appointment.model.TimeSlot;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FindAvailableSlotsService {

    private final FindAvailableSlotsUseCase findAvailableSlotsUseCase;

    public FindAvailableSlotsService(FindAvailableSlotsUseCase findAvailableSlotsUseCase) {
        this.findAvailableSlotsUseCase = findAvailableSlotsUseCase;
    }

    public AvailableSlotsResponse findAvailableSlots(LocalDate from, LocalDate to) {
        FindAvailableSlotsQuery query = new FindAvailableSlotsQuery(from, to);
        List<TimeSlot> slots = findAvailableSlotsUseCase.findAvailableSlots(query);
        return AvailableSlotsResponseMapper.toResponse(from, to, slots);
    }
}
