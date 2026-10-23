package ld.application.problems.server;

import ld.application.problems.server.ServerProblem.InternalError;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

public enum ServerErrorCode implements ApiErrorCode {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", InternalError.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    ServerErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}