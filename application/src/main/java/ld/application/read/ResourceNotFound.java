package ld.application.read;

import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.ErrorCode;
import ld.standard.lib.validation.FailureDetail;

import java.util.UUID;

public record ResourceNotFound(ErrorCode errorCode, String resourceLabel, UUID id) implements DomainError {

    @Override
    public FailureDetail detail() {
        return FailureDetail.resourceNotFound(
                errorCode,
                resourceLabel + " introuvable",
                "%s %s est introuvable".formatted(resourceLabel, id));
    }
}