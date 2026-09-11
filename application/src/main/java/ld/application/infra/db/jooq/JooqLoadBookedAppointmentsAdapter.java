package ld.application.infra.db.jooq;

import ld.domain.features.appointment.availability.LoadBookedAppointmentsPort;
import ld.domain.features.appointment.model.AppointmentState;
import ld.domain.features.appointment.model.TimeSlot;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

import static ld.application.jooq.tables.Appointment.APPOINTMENT;

@Repository
public class JooqLoadBookedAppointmentsAdapter implements LoadBookedAppointmentsPort {

    private final DSLContext dslContext;

    public JooqLoadBookedAppointmentsAdapter(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public List<TimeSlot> loadBookedSlots(ZonedDateTime from, ZonedDateTime to) {
        return dslContext.select(APPOINTMENT.START_AT, APPOINTMENT.END_AT)
                .from(APPOINTMENT)
                .where(APPOINTMENT.STATUS.eq(AppointmentState.SUBMITTED.name()))
                .and(APPOINTMENT.START_AT.lessThan(to.toOffsetDateTime()))
                .and(APPOINTMENT.END_AT.greaterThan(from.toOffsetDateTime()))
                .fetch(record -> new TimeSlot(
                        record.get(APPOINTMENT.START_AT).toZonedDateTime(),
                        record.get(APPOINTMENT.END_AT).toZonedDateTime()
                ));
    }
}