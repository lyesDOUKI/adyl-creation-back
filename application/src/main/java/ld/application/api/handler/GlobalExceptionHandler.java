package ld.application.api.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import ld.application.infra.db.jooq.exception.InvalidSortFieldException;
import ld.application.problems.request.RequestProblem;
import ld.application.problems.security.SecurityProblem;
import ld.application.problems.server.ServerProblem;
import ld.spring.web.lib.ApiProblem;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        var errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> violation(
                        error instanceof org.springframework.validation.FieldError fe
                                ? fe.getField() : error.getObjectName(),
                        error.getDefaultMessage()))
                .toList();
        return problem(new RequestProblem.ValidationFailed(errors));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        var errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> violation(result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return problem(new RequestProblem.ValidationFailed(errors));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        return problem(new RequestProblem.ValidationFailed(
                List.of(violation(ex.getRequestPartName(), "Partie obligatoire manquante."))));
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            @NonNull ServletRequestBindingException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        if (ex instanceof MissingServletRequestParameterException missing) {
            return problem(new RequestProblem.InvalidParameter(
                    missing.getParameterName(), "paramètre obligatoire manquant."));
        }
        return problem(new RequestProblem.InvalidParameter("request", "requête mal formée."));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            @NonNull TypeMismatchException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        String name = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName() : ex.getPropertyName();
        return problem(new RequestProblem.InvalidParameter(
                Optional.ofNullable(name).orElse("parameter"), "valeur incompatible avec le type attendu."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        String reason = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(" ; "));
        return problem(new RequestProblem.InvalidParameter("parameter", reason));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Optional<JacksonException> jackson = findCause(ex);

        String field = jackson.map(this::fieldPath).orElse("body");
        String message = jackson.map(this::messageOf).orElse("Corps de requête absent ou illisible.");

        return problem(new RequestProblem.InvalidRequestBody(List.of(violation(field, message))));
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    ResponseEntity<Object> handleInvalidSortField(InvalidSortFieldException ex) {
        return problem(new RequestProblem.InvalidSortField(ex.getMessage()));
    }

    @ExceptionHandler(MultipartException.class)
    ResponseEntity<Object> handleMultipart() {
        return problem(new RequestProblem.InvalidMultipart("Requête multipart invalide."));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Object> handleAuthentication() {
        return problem(new SecurityProblem.Unauthenticated());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> handleAccessDenied() {
        return problem(new SecurityProblem.Forbidden());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception ex) {
        LOGGER.error("Unexpected error", ex);
        return problem(new ServerProblem.InternalError());
    }

    private static ResponseEntity<Object> problem(ApiProblem problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private static RequestProblem.FieldViolation violation(String field, String message) {
        return new RequestProblem.FieldViolation(
                Optional.ofNullable(field).orElse("body"),
                Optional.ofNullable(message).orElse("Valeur invalide."));
    }

    private String messageOf(JacksonException ex) {
        if (ex instanceof UnrecognizedPropertyException) {
            return "Champ inconnu.";
        }
        if (ex instanceof InvalidFormatException invalid) {
            return invalidFormatMessage(invalid);
        }
        if (ex instanceof StreamReadException) {
            return "JSON mal formé.";
        }
        return "Valeur incompatible avec le type attendu.";
    }

    private String invalidFormatMessage(InvalidFormatException ex) {
        Class<?> target = ex.getTargetType();
        if (target != null && target.isEnum()) {
            String accepted = Arrays.stream(target.getEnumConstants())
                    .map(value -> ((Enum<?>) value).name())
                    .collect(Collectors.joining(", "));
            return "Valeur invalide. Valeurs acceptées : " + accepted + ".";
        }
        return "Valeur incompatible avec le type attendu.";
    }

    private String fieldPath(JacksonException ex) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference reference : ex.getPath()) {
            String property = reference.getPropertyName();
            if (property != null) {
                if (!path.isEmpty()) path.append('.');
                path.append(property);
            } else if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.isEmpty() ? "body" : path.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> Optional<T> findCause(Throwable exception) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof JacksonException) {
                return Optional.of(((Class<T>) JacksonException.class).cast(current));
            }
            if (current.getCause() == current) break;
        }
        return Optional.empty();
    }
}