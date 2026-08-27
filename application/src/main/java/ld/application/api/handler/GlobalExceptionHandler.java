package ld.application.api.handler;

import ld.application.infra.db.read.jooq.exception.InvalidSortFieldException;
import ld.spring.web.lib.ProblemDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetailResponse handleValidation(MethodArgumentNotValidException ex) {
        var problemDetail = ProblemDetailResponse.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setProperty("errors", ex.getBindingResult()
                .getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message",
                        Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")))
                .toList());
        return problemDetail;
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ProblemDetailResponse handleInvalidSortField(InvalidSortFieldException ex) {
        var problemDetail = ProblemDetailResponse.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Invalid sort field");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
