package ld.application.infra.db.jooq;

import java.util.UUID;

public interface CustomerRepository {
    Customer register(UUID identitySubject, String email, String phone);
}
