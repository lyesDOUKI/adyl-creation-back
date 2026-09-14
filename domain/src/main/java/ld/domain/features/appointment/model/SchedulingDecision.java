package ld.domain.features.appointment.model;

public sealed interface SchedulingDecision {

    record Accepted(TimeSlot slot) implements SchedulingDecision {}

    record Rejected(TimeSlot slot, TimeSlot conflictingSlot) implements SchedulingDecision {}

    static SchedulingDecision accepted(TimeSlot slot) {
        return new Accepted(slot);
    }

    static SchedulingDecision rejected(TimeSlot slot, TimeSlot conflictingSlot) {
        return new Rejected(slot, conflictingSlot);
    }
}