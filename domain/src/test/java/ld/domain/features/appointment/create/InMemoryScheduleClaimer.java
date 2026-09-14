package ld.domain.features.appointment.create;



import ld.domain.features.appointment.model.ScheduleClaim;
import ld.domain.features.appointment.model.TimeSlot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class InMemoryScheduleClaimer implements ScheduleClaimer {

    private final List<TimeSlot> bookedSlots = new ArrayList<>();
    private int claimCount = 0;

    @Override
    public ScheduleClaim claimExclusiveAccess(LocalDate date, ZoneId zoneId) {
        this.claimCount++;
        return new ScheduleClaim(date, zoneId);
    }

    @Override
    public List<TimeSlot> bookedSlotsFor(ScheduleClaim claim) {
        return this.bookedSlots.stream()
                .filter(slot -> slot.start().toLocalDate().equals(claim.date()))
                .toList();
    }

    public void addBookedSlot(TimeSlot timeSlot) {
        this.bookedSlots.add(timeSlot);
    }

    public int bookedSlotCount() {
        return this.bookedSlots.size();
    }

    public int claimCount() {
        return this.claimCount;
    }
}
