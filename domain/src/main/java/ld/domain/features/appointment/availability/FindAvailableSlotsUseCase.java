package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.TimeSlot;

import java.util.List;

public interface FindAvailableSlotsUseCase {

    List<TimeSlot> findAvailableSlots(FindAvailableSlotsQuery query);
}