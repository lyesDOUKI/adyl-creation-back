package ld.application.response;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID identitySubject,
        String email,
        String phone
) {
}
