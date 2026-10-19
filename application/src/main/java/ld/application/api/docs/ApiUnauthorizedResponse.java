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
        responseCode = "401",
        description = "Authentification manquante ou invalide",
        content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemSchema.class),
                examples = @ExampleObject(
                        name = "Unauthorized",
                        summary = "Non authentifié",
                        value = """
                                {
                                  "type": "urn:problem-type:unauthorized",
                                  "failureType": "AUTHENTICATION",
                                  "code": "UNAUTHORIZED",
                                  "title": "Unauthorized",
                                  "detail": "Authentication is required or the token is invalid",
                                  "status": 401,
                                  "instance": "/resource"
                                }
                                """
                )
        )
)
public @interface ApiUnauthorizedResponse {
}