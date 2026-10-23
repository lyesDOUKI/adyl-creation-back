package ld.application.problems.request;

import ld.application.problems.request.RequestProblem.InvalidRequestBody;
import ld.application.problems.request.RequestProblem.InvalidMultipart;
import ld.application.problems.request.RequestProblem.InvalidSortField;
import ld.application.problems.request.RequestProblem.ValidationFailed;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public enum RequestErrorCode implements ApiErrorCode {

    VALIDATION_FAILED(BAD_REQUEST, "Validation échouée", ValidationFailed.class),
    INVALID_REQUEST_BODY(BAD_REQUEST, "Requête invalide", InvalidRequestBody.class),
    INVALID_SORT_FIELD(BAD_REQUEST, "Champ de tri invalide", InvalidSortField.class),
    INVALID_MULTIPART_REQUEST(BAD_REQUEST, "Requête multipart invalide", InvalidMultipart.class),
    INVALID_PARAMETER(BAD_REQUEST, "Paramètre invalide",RequestProblem.InvalidParameter .class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    RequestErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Class<? extends ApiProblem> payload() {
        return payload;
    }
}