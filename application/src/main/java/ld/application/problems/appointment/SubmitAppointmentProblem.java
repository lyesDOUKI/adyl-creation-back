package ld.application.problems.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.application.response.TimeSlotResponse;
import ld.domain.features.appointment.create.SubmitAppointmentError;
import ld.spring.web.lib.ApiProblem;

import static ld.application.problems.appointment.SubmitAppointmentErrorCode.SLOT_ALREADY_BOOKED;
import static ld.application.problems.appointment.SubmitAppointmentErrorCode.SLOT_OUTSIDE_OPENING_HOURS;

public sealed interface SubmitAppointmentProblem extends ApiProblem {

    record SlotOutsideOpeningHours(
            @Schema(description = "Créneau demandé") TimeSlotResponse requestedSlot
    ) implements SubmitAppointmentProblem {
        @Override public SubmitAppointmentErrorCode getCode() { return SLOT_OUTSIDE_OPENING_HOURS; }
        @Override public String getDetail() { return "Le créneau demandé est hors des horaires autorisés."; }
    }

    record SlotAlreadyBooked(
            @Schema(description = "Créneau demandé") TimeSlotResponse requestedSlot
    ) implements SubmitAppointmentProblem {
        @Override public SubmitAppointmentErrorCode getCode() { return SLOT_ALREADY_BOOKED; }
        @Override public String getDetail() { return "Ce créneau est déjà réservé."; }
    }

    static SubmitAppointmentProblem from(SubmitAppointmentError error) {
        return switch (error) {
            case SubmitAppointmentError.SlotOutsideOpeningHours e ->
                    new SlotOutsideOpeningHours(TimeSlotResponse.from(e.requestedSlot()));
            case SubmitAppointmentError.SlotAlreadyBooked e ->
                    new SlotAlreadyBooked(TimeSlotResponse.from(e.requestedSlot()));
        };
    }
}