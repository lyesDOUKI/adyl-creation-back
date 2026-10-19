package ld.spring.web.lib;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ld.standard.lib.validation.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

@JsonPropertyOrder({
        "type",
        "failureType",
        "code",
        "title",
        "detail",
        "status",
        "instance"
})
public final class ProblemDetailResponse extends ProblemDetail implements ApiResponseBody {

    private final String code;
    private final FailureType failureType;

    private ProblemDetailResponse(HttpStatus status, FailureType failureType, String code) {
        super(status.value());
        this.failureType = failureType;
        this.code = code;
    }

    public static ProblemDetailResponse of(
            HttpStatus status,
            FailureType failureType,
            ErrorCode code,
            String title,
            String detail) {

        var problem = new ProblemDetailResponse(status, failureType, code.code());
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setType(URI.create("urn:problem-type:" + code.code().toLowerCase().replace('_', '-')));
        return problem;
    }

    public String getCode() {
        return code;
    }

    public FailureType getFailureType() {
        return failureType;
    }
}