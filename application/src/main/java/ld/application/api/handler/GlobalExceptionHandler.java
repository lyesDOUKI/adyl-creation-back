package ld.application.api.handler;

import ld.application.infra.db.jooq.exception.InvalidSortFieldException;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.Problems;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetailResponse handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")))
                .toList();

        return Problems.badRequest(
                RequestErrorCode.VALIDATION_FAILED,
                "Validation failed",
                "One or more fields are invalid",
                Map.of("errors", errors));
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ProblemDetailResponse handleInvalidSortField(InvalidSortFieldException ex) {
        return Problems.badRequest(
                RequestErrorCode.INVALID_SORT_FIELD,
                "Invalid sort field",
                ex.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetailResponse handleMultipartException(MultipartException ex) {
        return Problems.badRequest(
                RequestErrorCode.INVALID_MULTIPART_REQUEST,
                "Invalid multipart request",
                ex.getMessage());
    }
}