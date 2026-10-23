package ld.application.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiResponseBody;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Client enregistré")
public record CustomerResponse(
        @Schema(description = "Identifiant du client", requiredMode = REQUIRED)
        UUID id,

        @Schema(description = "Identifiant de l'identité (claim `sub` du jeton JWT)", requiredMode = REQUIRED)
        UUID identitySubject,

        @Schema(description = "Adresse e-mail du client", example = "marie@example.com", requiredMode = REQUIRED)
        String email,

        @Schema(description = "Numéro de téléphone du client", example = "+33612345678", requiredMode = REQUIRED)
        String phone
) implements ApiResponseBody {
}