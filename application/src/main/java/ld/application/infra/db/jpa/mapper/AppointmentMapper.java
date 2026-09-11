package ld.application.infra.db.jpa.mapper;

import ld.application.infra.db.entity.AppointmentEntity;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import ld.domain.features.appointment.model.AppointmentState;
import ld.domain.features.appointment.model.AppointmentStatus;

public class AppointmentMapper {

    private AppointmentMapper() {}

    public static AppointmentEntity toEntity(AppointmentSnapshot snapshot) {
        return switch (snapshot.appointmentStatus()) {
            case AppointmentStatus.Submitted submitted -> new AppointmentEntity(
                    snapshot.appointmentId(),
                    snapshot.start(),
                    snapshot.end(),
                    snapshot.identitySubject(),
                    AppointmentState.SUBMITTED,
                    submitted.submittedAt(),
                    null,
                    null
            );
        };
    }
}
