package ld.domain.features.appointment.model;

import java.time.LocalDate;
import java.time.ZoneId;

public record ScheduleClaim(LocalDate date, ZoneId zoneId) {
}
