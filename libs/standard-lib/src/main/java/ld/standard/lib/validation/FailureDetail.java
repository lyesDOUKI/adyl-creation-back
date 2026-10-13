package ld.standard.lib.validation;

public record FailureDetail(FailureType failureType, ErrorCode errorCode, String title, String message)
        implements DomainError {

    public static FailureDetail businessRule(ErrorCode code, String title, String message) {
        return new FailureDetail(FailureType.BUSINESS_RULE, code, title, message);
    }

    public static FailureDetail resourceNotFound(ErrorCode code, String title, String message) {
        return new FailureDetail(FailureType.RESOURCE_NOT_FOUND, code, title, message);
    }

    @Override
    public FailureDetail detail() {
        return this;
    }
}
