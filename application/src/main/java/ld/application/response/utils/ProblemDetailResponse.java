package ld.application.response.utils;

public record ProblemDetailResponse(String type, String title, int status, String detail, String instance) implements ApiResponseBody {
}
