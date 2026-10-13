package ld.domain.features.appointment.create;

import ld.domain.features.appointment.model.TimeSlot;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;

public sealed interface SubmitAppointmentError extends DomainError {

    record SlotOutsideOpeningHours(TimeSlot requestedSlot) implements SubmitAppointmentError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    AppointmentErrorCode.SLOT_OUTSIDE_OPENING_HOURS,
                    "Créneau hors horaires",
                    "Le créneau demandé est hors horaire autorisé");
        }
    }

    record SlotAlreadyBooked(TimeSlot requestedSlot) implements SubmitAppointmentError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    AppointmentErrorCode.SLOT_ALREADY_BOOKED,
                    "Créneau déjà réservé",
                    "Ce créneau est déjà réservé");
        }
    }
}
