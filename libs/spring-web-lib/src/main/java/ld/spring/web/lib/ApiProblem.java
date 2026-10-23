package ld.spring.web.lib;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"code", "title", "status", "detail"})
public interface ApiProblem extends ApiResponseBody {

    ApiErrorCode getCode();

    String getDetail();

    default String getTitle() { return getCode().title(); }
    default int getStatus()   { return getCode().status().value(); }
}