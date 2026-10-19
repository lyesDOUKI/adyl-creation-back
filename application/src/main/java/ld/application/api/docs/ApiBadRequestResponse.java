package ld.application.api.docs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "400",
        description = "Requête invalide (champs manquants, format incorrect, contraintes non respectées)",
        content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemSchema.class),
                examples = @ExampleObject(
                        name = "BadRequest",
                        summary = "Requête invalide",
                        value = """
                                {
                                  "type": "urn:problem-type:bad-request",
                                  "failureType": "INVALID_REQUEST",
                                  "code": "BAD_REQUEST",
                                  "title": "Bad request",
                                  "detail": "The request is invalid or contains invalid fields",
                                  "status": 400,
                                  "instance": "/resource",
                                  "errors": [
                                    {
                                      "field": "fieldName",
                                      "message": "The field does not satisfy the required constraints"
                                    }
                                  ]
                                }
                                """
                )
        )
)
public @interface ApiBadRequestResponse {
}