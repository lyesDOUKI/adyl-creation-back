package ld.domain.features.appointment.model;

import java.util.List;
import java.util.Objects;

public final class TimeSlots {

    private final List<TimeSlot> slots;

    public TimeSlots(List<TimeSlot> slots) {
        this.slots = List.copyOf(Objects.requireNonNull(slots, "slots must not be null"));
    }

    public TimeSlots excluding(TimeSlots occupied) {
        Objects.requireNonNull(occupied, "occupied must not be null");
        List<TimeSlot> remaining = slots.stream()
                .filter(slot -> occupied.slots.stream().noneMatch(slot::overlaps))
                .toList();
        return new TimeSlots(remaining);
    }

    public List<TimeSlot> asList() {
        return slots;
    }
}
