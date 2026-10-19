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

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "403",
        description = "Accès refusé (droits insuffisants)",
        content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemSchema.class),
                examples = @ExampleObject(
                        name = "Forbidden",
                        summary = "Accès refusé",
                        value = """
                                {
                                  "type": "urn:problem-type:forbidden",
                                  "failureType": "AUTHORIZATION",
                                  "code": "FORBIDDEN",
                                  "title": "Forbidden",
                                  "detail": "You do not have permission to access this resource",
                                  "status": 403,
                                  "instance": "/resource"
                                }
                                """
                )
        )
)
public @interface ApiForbiddenResponse {
}
