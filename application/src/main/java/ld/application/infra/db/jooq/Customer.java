package ld.application.infra.db.jooq;

import java.util.UUID;

public record Customer(
        UUID id,
        UUID identitySubject,
        String email,
        String phone
) {
}
