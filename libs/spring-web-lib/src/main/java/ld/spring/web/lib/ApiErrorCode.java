package ld.spring.web.lib;

import org.springframework.http.HttpStatus;

public interface ApiErrorCode {

    String name();
    HttpStatus status();
    String title();
    Class<? extends ApiProblem> payload();

    default String schemaName() {
        return useCase() + "_" + name();
    }

    default String description() { return title(); }

    private String useCase() {
        return getClass().getSimpleName().replace("ErrorCode", "");
    }
}