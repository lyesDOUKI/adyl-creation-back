package ld.lib.validation;

public record FailureDetail(FailureType type, ErrorCode errorCode, String title, String message) {
}
