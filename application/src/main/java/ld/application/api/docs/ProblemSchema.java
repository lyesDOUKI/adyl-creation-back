package ld.application.api.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.FailureType;

import java.net.URI;

@Schema(
        name = "Problem",
        description = "Erreur au format RFC 9457, retournée avec le content type application/problem+json. "
                + "Selon l'erreur, des membres supplémentaires peuvet s'ajouter à plat (ex : requestedSlot, errors).",
        additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
public record ProblemSchema(

        @Schema(description = "Identifiant stable de la catégorie du problème",
                example = "urn:problem-type:slot-already-booked")
        URI type,

        @Schema(description = "Famille de l'erreur : règle métier violée, ressource introuvable ou requête invalide",
                requiredMode = Schema.RequiredMode.REQUIRED)
        FailureType failureType,

        @Schema(description = "Code d'erreur stable, à utiliser côté client pour réagir à l'erreur",
                example = "SLOT_ALREADY_BOOKED",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Résumé court de l'erreur",
                example = "Créneau déjà réservé")
        String title,

        @Schema(description = "Explication de l'erreur pour cette occurrence",
                example = "Ce créneau est déjà réservé")
        String detail,

        @Schema(description = "Statut HTTP de la réponse",
                example = "409")
        int status,

        @Schema(description = "Chemin de la requête qui a provoqué l'erreur",
                example = "/appointments")
        URI instance
) { }
