package ld.spring.web.lib;

import ld.standard.lib.validation.ErrorCode;

import java.util.Map;

public interface ResourceKind {

    ErrorCode code();

    String title();

    String detailTemplate();

    default ProblemDetailResponse notFound(Object id) {
        return Problems.notFound(
                code(),
                title(),
                detailTemplate().formatted(id),
                Map.of("resourceId", id));
    }
}