package ld.spring.web.lib;

import jakarta.servlet.http.HttpServletRequest;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;
import ld.standard.lib.validation.FailureType;
import ld.standard.lib.validation.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;

public final class ResultToResponse {

    private ResultToResponse() {}

    public static <T extends ApiResponseBody, E extends DomainError> ResponseEntity<ApiResponseBody> ok(
            Result<T, E> result,
            HttpServletRequest request
    ) {
        return result.fold(
                ResponseEntity::ok,
                failureResponse(request)
        );
    }

    public static <T extends ApiResponseBody, E extends DomainError> ResponseEntity<ApiResponseBody> created(
            Result<T, E> result,
            Function<T, UUID> idExtractor,
            HttpServletRequest request
    ) {
        return result.fold(
                body -> ResponseEntity.created(buildLocation(request, idExtractor.apply(body))).body(body),
                failureResponse(request)
        );
    }

    public static <E extends DomainError> ResponseEntity<ApiResponseBody> noContent(
            Result<Void, E> result,
            HttpServletRequest request
    ) {
        return result.fold(
                _ -> ResponseEntity.noContent().build(),
                failureResponse(request)
        );
    }

    public static URI buildLocation(HttpServletRequest request, UUID id) {
        return ServletUriComponentsBuilder
                .fromRequestUri(request)
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }

    private static <E extends DomainError> Function<E, ResponseEntity<ApiResponseBody>> failureResponse(
            HttpServletRequest request
    ) {
        return error -> {
            FailureDetail detail = error.detail();
            HttpStatus status = toHttpStatus(detail.failureType());

            var problemDetail = ProblemDetailResponse.forStatus(
                    status,
                    detail.errorCode().code(),
                    detail.failureType().name()
            );
            problemDetail.setTitle(detail.title());
            problemDetail.setDetail(detail.message());
            problemDetail.setInstance(URI.create(request.getRequestURI()));

            return ResponseEntity.status(status).body(problemDetail);
        };
    }

    private static HttpStatus toHttpStatus(FailureType type) {
        return switch (type) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
    }
}