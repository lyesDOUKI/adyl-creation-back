package ld.domain.features.appointment.create;

import ld.domain.features.appointment.model.AppointmentSnapshot;

public interface AppointmentCreator {
    void create(AppointmentSnapshot appointmentSnapshot);
}