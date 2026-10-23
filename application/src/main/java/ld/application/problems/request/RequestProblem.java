package ld.application.problems.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiProblem;

import java.util.List;

import static ld.application.problems.request.RequestErrorCode.INVALID_MULTIPART_REQUEST;
import static ld.application.problems.request.RequestErrorCode.INVALID_REQUEST_BODY;
import static ld.application.problems.request.RequestErrorCode.INVALID_SORT_FIELD;
import static ld.application.problems.request.RequestErrorCode.VALIDATION_FAILED;
import static ld.application.problems.request.RequestErrorCode.INVALID_PARAMETER;
public sealed interface RequestProblem extends ApiProblem {

    record FieldViolation(
            @Schema(description = "Champ ou chemin du champ concerné dans le corps de la requête")
            String field,

            @Schema(description = "Explication lisible de l'erreur constatée pour ce champ")
            String message
    ) {}

    record ValidationFailed(
            @Schema(description = "Violations constatées, une par champ")
            List<FieldViolation> errors
    ) implements RequestProblem {
        @Override public RequestErrorCode getCode() { return VALIDATION_FAILED; }
        @Override public String getDetail() { return "Un ou plusieurs champs sont invalides."; }
    }

    record InvalidRequestBody(
            @Schema(description = """
                    Erreurs de lecture identifiées dans le corps de la requête.
                    La liste peut être vide lorsqu'aucun champ précis ne peut être identifié.
                    """)
            List<FieldViolation> errors
    ) implements RequestProblem {
        @Override public RequestErrorCode getCode() { return INVALID_REQUEST_BODY; }

        @Override public String getDetail() {
            return "Le corps de la requête est absent, mal formé ou contient une valeur incompatible avec le format attendu.";
        }
    }

    record InvalidSortField(
            @Schema(description = "Raison du refus")
            String reason
    ) implements RequestProblem {
        @Override public RequestErrorCode getCode() { return INVALID_SORT_FIELD; }
        @Override public String getDetail() { return reason; }
    }

    record InvalidMultipart(
            @Schema(description = "Raison du refus")
            String reason
    ) implements RequestProblem {
        @Override public RequestErrorCode getCode() { return INVALID_MULTIPART_REQUEST; }
        @Override public String getDetail() { return reason; }
    }

    record InvalidParameter(
            @Schema(description = "Nom du paramètre concerné") String parameter,
            @Schema(description = "Raison du refus") String reason
    ) implements RequestProblem {
        @Override public RequestErrorCode getCode() { return INVALID_PARAMETER; }
        @Override public String getDetail() {
            return "Le paramètre '%s' est invalide : %s".formatted(parameter, reason);
        }
    }
}