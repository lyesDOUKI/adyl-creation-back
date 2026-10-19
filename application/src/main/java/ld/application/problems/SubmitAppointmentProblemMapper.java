package ld.application.problems;

import ld.domain.features.appointment.create.AppointmentErrorCode;
import ld.domain.features.appointment.create.SubmitAppointmentError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

public class SubmitAppointmentProblemMapper implements ProblemMapper<SubmitAppointmentError> {

    @Override
    public ProblemDetailResponse toProblem(SubmitAppointmentError error) {
        return switch (error) {

            case SubmitAppointmentError.SlotOutsideOpeningHours e -> {
                var problem = Problems.unprocessable(
                        AppointmentErrorCode.SLOT_OUTSIDE_OPENING_HOURS,
                        "Créneau hors horaires",
                        "Le créneau demandé est hors horaire autorisé");
                problem.setProperty("requestedSlot", e.requestedSlot());
                yield problem;
            }

            case SubmitAppointmentError.SlotAlreadyBooked e -> {
                var problem = Problems.conflict(
                        AppointmentErrorCode.SLOT_ALREADY_BOOKED,
                        "Créneau déjà réservé",
                        "Ce créneau est déjà réservé");
                problem.setProperty("requestedSlot", e.requestedSlot());
                yield problem;
            }
        };
    }
}