package ld.spring.web.lib;

import jakarta.servlet.http.HttpServletRequest;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.Result;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class ResultToResponse {

    private ResultToResponse() { }

    public static <T extends ApiResponseBody, E extends DomainError> ResponseEntity<ApiResponseBody> ok(
            Result<T, E> result,
            ProblemMapper<E> problems,
            HttpServletRequest request) {

        return result.<ResponseEntity<ApiResponseBody>>fold(
                value -> ResponseEntity.ok(value),
                error -> problem(problems.toProblem(error), request));
    }

    public static <T extends ApiResponseBody, E extends DomainError> ResponseEntity<ApiResponseBody> created(
            Result<T, E> result,
            Function<T, UUID> idExtractor,
            ProblemMapper<E> problems,
            HttpServletRequest request) {

        return result.<ResponseEntity<ApiResponseBody>>fold(
                value -> {
                    URI location = ServletUriComponentsBuilder.fromRequest(request)
                            .path("/{id}")
                            .buildAndExpand(idExtractor.apply(value))
                            .toUri();
                    return ResponseEntity.created(location).body(value);
                },
                error -> problem(problems.toProblem(error), request));
    }

    public static <T extends ApiResponseBody> ResponseEntity<ApiResponseBody> okOrNotFound(
            Optional<T> found,
            ResourceKind kind,
            UUID id,
            HttpServletRequest request) {

        return found
                .<ResponseEntity<ApiResponseBody>>map(value -> ResponseEntity.ok(value))
                .orElseGet(() -> problem(kind.notFound(id), request));
    }


    private static ResponseEntity<ApiResponseBody> problem(ProblemDetailResponse problem,
                                                           HttpServletRequest request) {
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}