package ld.application.problems.security;

import ld.application.problems.security.SecurityProblem.Forbidden;
import ld.application.problems.security.SecurityProblem.Unauthenticated;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

public enum SecurityErrorCode implements ApiErrorCode {

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Non authentifié", Unauthenticated.class),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Accès refusé", Forbidden.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    SecurityErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}