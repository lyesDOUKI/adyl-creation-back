package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.TimeSlot;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryLoadBookedAppointments implements LoadBookedAppointmentsPort {

    private final List<TimeSlot> bookedSlots = new ArrayList<>();
    private ZonedDateTime lastRequestedFrom;
    private ZonedDateTime lastRequestedTo;

    public void addBookedSlot(TimeSlot slot) {
        bookedSlots.add(slot);
    }

    @Override
    public List<TimeSlot> loadBookedSlots(ZonedDateTime from, ZonedDateTime to) {
        this.lastRequestedFrom = from;
        this.lastRequestedTo = to;
        return bookedSlots.stream()
                .filter(slot -> slot.start().isBefore(to) && from.isBefore(slot.end()))
                .toList();
    }

    ZonedDateTime lastRequestedFrom() {
        return lastRequestedFrom;
    }

    ZonedDateTime lastRequestedTo() {
        return lastRequestedTo;
    }
}
