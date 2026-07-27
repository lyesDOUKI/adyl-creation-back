package ld.application.response.utils;

import jakarta.servlet.http.HttpServletRequest;
import ld.lib.validation.FailureDetail;
import ld.lib.validation.FailureType;
import ld.lib.validation.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;

public final class ResultToResponse {

    private ResultToResponse() {}

    public static <T extends ApiResponseBody> ResponseEntity<ApiResponseBody> ok(
            Result<T> result,
            HttpServletRequest request
    ) {
        return result.resolve(ResponseEntity::ok, failureResponse(request));
    }

    public static <T extends ApiResponseBody> ResponseEntity<ApiResponseBody> created(
            Result<T> result,
            Function<T, UUID> idExtractor,
            HttpServletRequest request
    ) {
        return result.resolve(
                body -> ResponseEntity.created(buildLocation(request, idExtractor.apply(body))).body(body),
                failureResponse(request)
        );
    }

    public static ResponseEntity<ApiResponseBody> noContent(
            Result<Void> result,
            HttpServletRequest request
    ) {
        return result.resolve(_ -> ResponseEntity.noContent().build(), failureResponse(request));
    }

    private static URI buildLocation(HttpServletRequest request, UUID id) {
        return ServletUriComponentsBuilder
                .fromRequestUri(request)
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }

    private static Function<FailureDetail, ResponseEntity<ApiResponseBody>> failureResponse(
            HttpServletRequest request
    ) {
        return detail -> {
            HttpStatus status = toHttpStatus(detail.type());
            return ResponseEntity
                    .status(status)
                    .body(new ProblemDetailResponse(
                            detail.type().name(),
                            detail.errorCode().code(),
                            detail.title(),
                            detail.message(),
                            status.value(),
                            request.getRequestURI()
                    ));
        };
    }

    private static HttpStatus toHttpStatus(FailureType type) {
        return switch (type) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
    }
}