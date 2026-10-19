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
        responseCode = "404",
        description = "Ressource introuvable",
        content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemSchema.class),
                examples = @ExampleObject(
                        name = "ResourceNotFound",
                        summary = "Ressource introuvable",
                        value = """
                                {
                                  "type": "urn:problem-type:resource-not-found",
                                  "failureType": "RESOURCE_NOT_FOUND",
                                  "code": "RESOURCE_NOT_FOUND",
                                  "title": "Resource not found",
                                  "detail": "The requested resource does not exist",
                                  "status": 404,
                                  "instance": "/resource/{id}"
                                }
                                """
                )
        )
)
public @interface ApiResourceNotFoundResponse {
}
