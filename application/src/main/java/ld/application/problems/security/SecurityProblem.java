package ld.application.problems.security;

import ld.spring.web.lib.ApiProblem;

import static ld.application.problems.security.SecurityErrorCode.FORBIDDEN;
import static ld.application.problems.security.SecurityErrorCode.UNAUTHENTICATED;

public sealed interface SecurityProblem extends ApiProblem {

    record Unauthenticated() implements SecurityProblem {
        @Override public SecurityErrorCode getCode() { return UNAUTHENTICATED; }
        @Override public String getDetail() { return "Authentification manquante ou invalide."; }
    }

    record Forbidden() implements SecurityProblem {
        @Override public SecurityErrorCode getCode() { return FORBIDDEN; }
        @Override public String getDetail() { return "Vous n'avez pas les droits pour accéder à cette ressource."; }
    }
}