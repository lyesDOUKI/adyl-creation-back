package ld.application.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterCustomerRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email,
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
        String phone
) {
}