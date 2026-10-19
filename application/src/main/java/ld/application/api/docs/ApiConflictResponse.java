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
        responseCode = "409",
        description = "Conflit avec l'état actuel de la ressource (règle métier)",
        content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemSchema.class),
                examples = @ExampleObject(
                        name = "Conflict",
                        summary = "Conflit",
                        value = """
                                {
                                  "type": "urn:problem-type:conflict",
                                  "failureType": "STATE_CONFLICT",
                                  "code": "CONFLICT",
                                  "title": "Conflict",
                                  "detail": "The request conflicts with the current state of the resource",
                                  "status": 409,
                                  "instance": "/resource/{id}"
                                }
                                """
                )
        )
)
public @interface ApiConflictResponse {
}