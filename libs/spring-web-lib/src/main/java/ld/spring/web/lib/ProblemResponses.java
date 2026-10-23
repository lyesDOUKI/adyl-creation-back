package ld.spring.web.lib;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ProblemResponses {
    private ProblemResponses() {}

    public static ResponseEntity<ApiResponseBody> of(ApiProblem problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}