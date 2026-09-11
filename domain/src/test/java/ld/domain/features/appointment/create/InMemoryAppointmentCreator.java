package ld.domain.features.appointment.create;

import ld.domain.features.appointment.model.AppointmentSnapshot;

import java.util.ArrayList;
import java.util.List;

class InMemoryAppointmentCreator implements AppointmentCreator {

    private final List<AppointmentSnapshot> appointments = new ArrayList<>();

    @Override
    public void create(AppointmentSnapshot appointmentSnapshot) {
        appointments.add(appointmentSnapshot);
    }

    int count() {
        return appointments.size();
    }

    List<AppointmentSnapshot> appointments() {
        return appointments;
    }
}
