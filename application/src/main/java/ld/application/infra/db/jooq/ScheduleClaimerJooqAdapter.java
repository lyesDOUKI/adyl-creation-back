package ld.application.infra.db.jooq;

import ld.domain.features.appointment.create.ScheduleClaimer;
import ld.domain.features.appointment.model.AppointmentState;
import ld.domain.features.appointment.model.ScheduleClaim;
import ld.domain.features.appointment.model.TimeSlot;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static ld.application.jooq.tables.Appointment.APPOINTMENT;
import static ld.application.jooq.tables.ScheduleClaim.SCHEDULE_CLAIM;

@Repository
public class ScheduleClaimerJooqAdapter implements ScheduleClaimer {

    private final DSLContext dslContext;

    public ScheduleClaimerJooqAdapter(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public ScheduleClaim claimExclusiveAccess(LocalDate date, ZoneId zone) {

        dslContext
                .insertInto(SCHEDULE_CLAIM)
                .set(SCHEDULE_CLAIM.CLAIM_DATE, date)
                .onConflict(SCHEDULE_CLAIM.CLAIM_DATE)
                .doNothing()
                .execute();

        dslContext
                .selectFrom(SCHEDULE_CLAIM)
                .where(SCHEDULE_CLAIM.CLAIM_DATE.eq(date))
                .forUpdate()
                .fetchSingle();

        return new ScheduleClaim(date, zone);
    }

    @Override
    public List<TimeSlot> bookedSlotsFor(ScheduleClaim claim) {

        LocalDate date = claim.date();

        ZonedDateTime dayStart = date.atStartOfDay(claim.zoneId());
        ZonedDateTime dayEnd = date.plusDays(1).atStartOfDay(claim.zoneId());

        return dslContext
                .select(APPOINTMENT.START_AT, APPOINTMENT.END_AT)
                .from(APPOINTMENT)
                .where(APPOINTMENT.STATUS.ne(AppointmentState.CANCELLED.name()))
                .and(APPOINTMENT.START_AT.lt(dayEnd.toOffsetDateTime()))
                .and(APPOINTMENT.END_AT.gt(dayStart.toOffsetDateTime()))
                .fetch(record -> new TimeSlot(
                        record.get(APPOINTMENT.START_AT).toZonedDateTime(),
                        record.get(APPOINTMENT.END_AT).toZonedDateTime()
                ));
    }
}
