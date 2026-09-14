package ld.domain.features.appointment.model;

import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

public class Appointment extends AggregateRoot<UUID, AppointmentEvent> implements Snapshottable<AppointmentSnapshot> {

    private final TimeSlot timeSlot;
    private final UUID identitySubject;
    private final AppointmentStatus appointmentStatus;
    private final AppointmentNote appointmentNote;
    private Appointment(TimeSlot timeSlot, UUID identitySubject, Clock clock, AppointmentNote appointmentNote) {
        this.appointmentNote = appointmentNote;
        this.setId(UUID.randomUUID());
        this.timeSlot = timeSlot;
        this.identitySubject = identitySubject;
        this.appointmentStatus = new AppointmentStatus.Submitted(clock.instant());
        this.addDomainEvent(new AppointmentSubmitted(getId(), timeSlot.start(), timeSlot.end(), identitySubject));
    }

    public Appointment(UUID appointmentId, TimeSlot timeSlot, UUID identitySubject,
                       AppointmentStatus appointmentStatus, AppointmentNote appointmentNote) {
        this.timeSlot = timeSlot;
        this.appointmentNote = appointmentNote;
        setId(appointmentId);
        this.identitySubject = identitySubject;
        this.appointmentStatus = appointmentStatus;
    }

    public static Appointment create(TimeSlot timeSlot, UUID identitySubject, String note, Clock clock) {
        return new Appointment(timeSlot, identitySubject, clock,
                Optional.ofNullable(note)
                        .filter(notes -> !notes.isBlank())
                        .map(AppointmentNote::new).orElse(null));
    }

    public static Appointment from(AppointmentSnapshot snapshot) {
        return new Appointment(
                snapshot.appointmentId(),
                snapshot.timeSlot(),
                snapshot.identitySubject(),
                snapshot.appointmentStatus(),
                snapshot.note().orElse(null));
    }

    @Override
    public AppointmentSnapshot toSnapshot() {
        return new AppointmentSnapshot(
                this.getId(),
                this.timeSlot,
                this.identitySubject,
                this.appointmentStatus,
                Optional.ofNullable(this.appointmentNote)
        );
    }
}
