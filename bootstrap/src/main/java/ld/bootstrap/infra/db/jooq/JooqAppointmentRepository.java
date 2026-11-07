package ld.bootstrap.infra.db.jooq;

import ld.bootstrap.jooq.tables.records.AppointmentRecord;
import ld.domain.features.appointment.model.AppointmentState;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static ld.bootstrap.jooq.tables.Appointment.APPOINTMENT;

@Repository
public class JooqAppointmentRepository implements AppointmentRepository {

    private final DSLContext dslContext;

    public JooqAppointmentRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public List<AppointmentQuery> findAll(UUID identitySubject) {
        return dslContext.selectFrom(APPOINTMENT)
                .where(APPOINTMENT.IDENTITY_SUBJECT.eq(identitySubject))
                .orderBy(APPOINTMENT.START_AT.desc())
                .fetch()
                .map(this::toAppointmentQuery);
    }

    private AppointmentQuery toAppointmentQuery(AppointmentRecord record) {
        return new AppointmentQuery(
                record.getId(),
                record.getStartAt(),
                record.getEndAt(),
                record.getIdentitySubject(),
                AppointmentState.valueOf(record.getStatus()),
                record.getSubmittedAt(),
                record.getCancelledAt(),
                record.getCancelledReason(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}