package ld.domain.features.appointment.create;

import ld.domain.features.appointment.model.TimeSlot;
import ld.standard.lib.validation.DomainError;

public sealed interface SubmitAppointmentError extends DomainError {

    record SlotOutsideOpeningHours(TimeSlot requestedSlot) implements SubmitAppointmentError {}

    record SlotAlreadyBooked(TimeSlot requestedSlot) implements SubmitAppointmentError {}
}
