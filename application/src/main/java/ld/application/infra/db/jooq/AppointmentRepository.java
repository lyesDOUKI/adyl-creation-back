package ld.application.infra.db.jooq;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepository {
    List<AppointmentQuery> findAll(UUID identitySubject);
}
