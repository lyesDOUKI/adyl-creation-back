package ld.spring.web.lib;

import ld.standard.lib.validation.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class Problems {

    private Problems() { }

    public static ProblemDetailResponse notFound(ErrorCode code, String title, String detail) {
        return ProblemDetailResponse.of(HttpStatus.NOT_FOUND, FailureType.RESOURCE_NOT_FOUND, code, title, detail);
    }

    public static ProblemDetailResponse notFound(ErrorCode code, String title, String detail,
                                                 Map<String, ?> properties) {
        return withProperties(notFound(code, title, detail), properties);
    }

    public static ProblemDetailResponse conflict(ErrorCode code, String title, String detail) {
        return ProblemDetailResponse.of(HttpStatus.CONFLICT, FailureType.STATE_CONFLICT, code, title, detail);
    }

    public static ProblemDetailResponse conflict(ErrorCode code, String title, String detail,
                                                 Map<String, ?> properties) {
        return withProperties(conflict(code, title, detail), properties);
    }

    public static ProblemDetailResponse unprocessable(ErrorCode code, String title, String detail) {
        return ProblemDetailResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, FailureType.BUSINESS_RULE,
                code, title, detail);
    }

    public static ProblemDetailResponse unprocessable(ErrorCode code, String title, String detail,
                                                      Map<String, ?> properties) {
        return withProperties(unprocessable(code, title, detail), properties);
    }

    public static ProblemDetailResponse badRequest(ErrorCode code, String title, String detail) {
        return ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, FailureType.INVALID_REQUEST, code, title, detail);
    }

    public static ProblemDetailResponse badRequest(ErrorCode code, String title, String detail,
                                                   Map<String, ?> properties) {
        return withProperties(badRequest(code, title, detail), properties);
    }
    private static ProblemDetailResponse withProperties(ProblemDetailResponse problem,
                                                        Map<String, ?> properties) {
        properties.forEach(problem::setProperty);
        return problem;
    }
}