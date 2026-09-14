package ld.domain.features.appointment.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class DailySchedule {

    private final ScheduleClaim claim;
    private final List<TimeSlot> bookedSlots;

    public DailySchedule(ScheduleClaim claim, List<TimeSlot> bookedSlots) {
        this.claim = Objects.requireNonNull(claim);
        this.bookedSlots = List.copyOf(bookedSlots);
    }

    public SchedulingDecision attemptToBook(TimeSlot requested) {
        return bookedSlots.stream()
                .filter(requested::overlaps)
                .findFirst()
                .map(conflicting -> SchedulingDecision.rejected(requested, conflicting))
                .orElseGet(() -> SchedulingDecision.accepted(requested));
    }

    public LocalDate date() {
        return claim.date();
    }
}