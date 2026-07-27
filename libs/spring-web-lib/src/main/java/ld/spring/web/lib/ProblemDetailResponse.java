package ld.spring.web.lib;

public record ProblemDetailResponse(String type, String code, String title, String detail, int status, String instance) implements ApiResponseBody {
}
