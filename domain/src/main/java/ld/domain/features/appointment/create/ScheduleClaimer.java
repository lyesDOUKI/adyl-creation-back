package ld.domain.features.appointment.create;

import ld.domain.features.appointment.model.ScheduleClaim;
import ld.domain.features.appointment.model.TimeSlot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface ScheduleClaimer {
    ScheduleClaim claimExclusiveAccess(LocalDate date, ZoneId zone);
    List<TimeSlot> bookedSlotsFor(ScheduleClaim claim);
}
