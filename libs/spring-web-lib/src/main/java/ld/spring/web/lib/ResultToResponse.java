package ld.spring.web.lib;

import jakarta.servlet.http.HttpServletRequest;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ResultToResponse {

    private ResultToResponse() { }

    public static <T extends ApiResponseBody, E extends DomainError> ResponseEntity<ApiResponseBody> ok(
            Result<T, E> result,
            Function<E, ? extends ApiProblem> toProblem) {

        return result.fold(
                ResponseEntity::ok,
                error -> ProblemResponses.of(toProblem.apply(error)));
    }

    public static <T extends ApiResponseBody, E extends DomainError> ResponseEntity<ApiResponseBody> created(
            Result<T, E> result,
            Function<T, UUID> idExtractor,
            Function<E, ? extends ApiProblem> toProblem,
            HttpServletRequest request) {

        return result.fold(
                value -> {
                    URI location = ServletUriComponentsBuilder.fromRequest(request)
                            .path("/{id}")
                            .buildAndExpand(idExtractor.apply(value))
                            .toUri();
                    return ResponseEntity.created(location).body(value);
                },
                error -> ProblemResponses.of(toProblem.apply(error)));
    }

    public static <T extends ApiResponseBody> ResponseEntity<ApiResponseBody> okOrNotFound(
            Optional<T> found,
            Supplier<? extends ApiProblem> notFound) {

        return found
                .<ResponseEntity<ApiResponseBody>>map(ResponseEntity::ok)
                .orElseGet(() -> ProblemResponses.of(notFound.get()));
    }
}