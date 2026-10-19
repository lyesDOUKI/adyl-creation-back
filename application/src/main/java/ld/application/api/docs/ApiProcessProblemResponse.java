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
        responseCode = "422",
        description = "Traitement impossible (règle métier non respectée)",
        content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemSchema.class),
                examples = @ExampleObject(
                        name = "ProcessProblem",
                        summary = "Règle métier non respectée",
                        value = """
                                {
                                  "type": "urn:problem-type:business-rule-violation",
                                  "failureType": "BUSINESS_RULE",
                                  "code": "BUSINESS_RULE_VIOLATION",
                                  "title": "Unprocessable request",
                                  "detail": "The request is valid but cannot be processed due to a business rule",
                                  "status": 422,
                                  "instance": "/resource{id}"
                                }
                                """
                )
        )
)
public @interface ApiProcessProblemResponse {
}