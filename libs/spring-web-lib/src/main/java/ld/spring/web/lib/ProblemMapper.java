package ld.spring.web.lib;

import ld.standard.lib.validation.DomainError;

@FunctionalInterface
public interface ProblemMapper<E extends DomainError> {
    ProblemDetailResponse toProblem(E error);
}