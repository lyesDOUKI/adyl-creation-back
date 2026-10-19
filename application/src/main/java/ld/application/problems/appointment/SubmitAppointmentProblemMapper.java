package ld.application.problems.appointment;

import ld.domain.features.appointment.create.SubmitAppointmentError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.ProblemSpecs;

public class SubmitAppointmentProblemMapper implements ProblemMapper<SubmitAppointmentError> {

    @Override
    public ProblemDetailResponse toProblem(SubmitAppointmentError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private SubmitAppointmentProblem toApi(SubmitAppointmentError error) {
        return switch (error) {
            case SubmitAppointmentError.SlotOutsideOpeningHours e ->
                    new SubmitAppointmentProblem.slotOutsideOpeningHours(e.requestedSlot());

            case SubmitAppointmentError.SlotAlreadyBooked e ->
                    new SubmitAppointmentProblem.slotAlreadyBooked(e.requestedSlot());
        };
    }
}