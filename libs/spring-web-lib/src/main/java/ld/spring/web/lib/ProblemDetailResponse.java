package ld.spring.web.lib;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@JsonPropertyOrder({
        "failureType",
        "code",
        "title",
        "detail",
        "status",
        "instance"
})
public final class ProblemDetailResponse extends ProblemDetail
        implements ApiResponseBody {

    private String code;

    private String failureType;

    private ProblemDetailResponse(
            HttpStatus status,
            String code,
            String failureType
    ) {
        super(status.value());

        this.code = code;
        this.failureType = failureType;
    }


    public static ProblemDetailResponse forStatus(
            HttpStatus status,
            String code,
            String failureType
    ) {
        return new ProblemDetailResponse(
                status,
                code,
                failureType
        );
    }

    private ProblemDetailResponse(HttpStatus status) {
        super(status.value());
    }

    public static ProblemDetailResponse forStatus(HttpStatus status) {
        return new ProblemDetailResponse(status);
    }

    public String getCode() {
        return code;
    }

    public String getFailureType() {
        return failureType;
    }
}