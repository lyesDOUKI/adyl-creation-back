package ld.standard.lib.validation;

public record FailureDetail(FailureType failureType, ErrorCode errorCode, String title, String message) {
}
