package ld.application.problems.server;

import ld.spring.web.lib.ApiProblem;

public sealed interface ServerProblem extends ApiProblem {

    record InternalError() implements ServerProblem {
        @Override public ServerErrorCode getCode() { return ServerErrorCode.INTERNAL_ERROR; }
        @Override public String getDetail() { return "Une erreur inattendue s'est produite."; }
    }
}