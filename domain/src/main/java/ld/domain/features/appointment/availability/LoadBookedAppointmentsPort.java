package ld.domain.features.appointment.availability;

import ld.domain.features.appointment.model.TimeSlot;

import java.time.ZonedDateTime;
import java.util.List;

public interface LoadBookedAppointmentsPort {
    List<TimeSlot> loadBookedSlots(ZonedDateTime from, ZonedDateTime to);
}
