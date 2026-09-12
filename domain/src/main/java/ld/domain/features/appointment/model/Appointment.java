package ld.domain.features.appointment.model;

import ld.standard.lib.AggregateRoot;
import ld.standard.lib.Snapshottable;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class Appointment extends AggregateRoot<UUID, AppointmentEvent> implements Snapshottable<AppointmentSnapshot> {

    private final ZonedDateTime start;
    private final ZonedDateTime end;
    private final UUID identitySubject;
    private final AppointmentStatus appointmentStatus;
    private final AppointmentNote appointmentNote;
    private Appointment(ZonedDateTime start, ZonedDateTime end, UUID identitySubject, Clock clock, AppointmentNote appointmentNote) {
        this.appointmentNote = appointmentNote;
        this.setId(UUID.randomUUID());
        this.start = start;
        this.end = end;
        this.identitySubject = identitySubject;
        this.appointmentStatus = new AppointmentStatus.Submitted(clock.instant());
        this.addDomainEvent(new AppointmentSubmitted(getId(), start, end, identitySubject));
    }

    public Appointment(UUID appointmentId, ZonedDateTime start, ZonedDateTime end, UUID identitySubject,
                       AppointmentStatus appointmentStatus, AppointmentNote appointmentNote) {
        this.appointmentNote = appointmentNote;
        setId(appointmentId);
        this.start = start;
        this.end = end;
        this.identitySubject = identitySubject;
        this.appointmentStatus = appointmentStatus;
    }

    public static Appointment create(ZonedDateTime start, ZonedDateTime end, UUID identitySubject, String note, Clock clock) {
        return new Appointment(start, end, identitySubject, clock,
                Optional.ofNullable(note)
                        .filter(notes -> !notes.isBlank())
                        .map(AppointmentNote::new).orElse(null));
    }

    public static Appointment from(AppointmentSnapshot snapshot) {
        return new Appointment(
                snapshot.appointmentId(),
                snapshot.start(),
                snapshot.end(),
                snapshot.identitySubject(),
                snapshot.appointmentStatus(),
                snapshot.note().orElse(null)
        );
    }

    @Override
    public AppointmentSnapshot toSnapshot() {
        return new AppointmentSnapshot(
                this.getId(),
                this.start,
                this.end,
                this.identitySubject,
                this.appointmentStatus,
                Optional.ofNullable(this.appointmentNote)
        );
    }
}
