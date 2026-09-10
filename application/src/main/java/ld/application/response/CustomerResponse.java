package ld.application.response;

import ld.spring.web.lib.ApiResponseBody;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID identitySubject,
        String email,
        String phone
) implements ApiResponseBody {
}
