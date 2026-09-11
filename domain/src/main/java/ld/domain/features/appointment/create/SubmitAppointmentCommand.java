package ld.domain.features.appointment.create;

import java.time.ZonedDateTime;
import java.util.UUID;

public record SubmitAppointmentCommand(ZonedDateTime start, ZonedDateTime end, UUID identitySubject) {}