package ld.application.problems.appointment;

import ld.domain.features.appointment.model.TimeSlot;
import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.ConflictProblem;
import ld.spring.web.lib.UnprocessableProblem;

import java.util.Map;

public sealed interface SubmitAppointmentProblem extends ApiProblem {

    @AppointmentProblem(AppointmentErrorCode.SLOT_OUTSIDE_OPENING_HOURS)
    record slotOutsideOpeningHours(TimeSlot requestedSlot)
            implements SubmitAppointmentProblem, UnprocessableProblem {

        public String title() {
            return "Créneau hors horaires";
        }

        public String detail() {
            return "Le créneau demandé est hors horaire autorisé";
        }

        public Map<String, ?> properties() {
            return Map.of("requestedSlot", requestedSlot);
        }
    }

    @AppointmentProblem(AppointmentErrorCode.SLOT_ALREADY_BOOKED)
    record slotAlreadyBooked(TimeSlot requestedSlot)
            implements SubmitAppointmentProblem, ConflictProblem {

        public String title() {
            return "Créneau déjà réservé";
        }

        public String detail() {
            return "Ce créneau est déjà réservé";
        }

        public Map<String, ?> properties() {
            return Map.of("requestedSlot", requestedSlot);
        }
    }
}
