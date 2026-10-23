package ld.application.problems.appointment;

import ld.application.problems.appointment.SubmitAppointmentProblem.SlotAlreadyBooked;
import ld.application.problems.appointment.SubmitAppointmentProblem.SlotOutsideOpeningHours;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

public enum SubmitAppointmentErrorCode implements ApiErrorCode {

    SLOT_OUTSIDE_OPENING_HOURS(UNPROCESSABLE_CONTENT, "Créneau hors horaires", SlotOutsideOpeningHours.class),
    SLOT_ALREADY_BOOKED(CONFLICT, "Créneau déjà réservé", SlotAlreadyBooked.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    SubmitAppointmentErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}