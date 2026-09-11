package ld.domain.features.appointment.create;

import ld.standard.lib.validation.ErrorCode;

public enum AppointmentErrorCode implements ErrorCode {
    SLOT_OUTSIDE_OPENING_HOURS,
    SLOT_ALREADY_BOOKED;
}
