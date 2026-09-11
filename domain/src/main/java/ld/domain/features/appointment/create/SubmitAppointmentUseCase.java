package ld.domain.features.appointment.create;

import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.standard.lib.validation.Result;

public interface SubmitAppointmentUseCase {
    Result<AppointmentSnapshot> execute(SubmitAppointmentCommand submitAppointmentCommand);
}
