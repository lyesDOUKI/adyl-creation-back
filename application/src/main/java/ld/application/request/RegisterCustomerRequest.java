package ld.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Enregistrement du client authentifié")
public record RegisterCustomerRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        @Schema(description = "Adresse e-mail du client", example = "marie@example.com")
        String email,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
        @Schema(description = "Numéro de téléphone, avec ou sans « + », de 10 à 15 chiffres",
                example = "+33612345678")
        String phone
) {
}