package ld.standard.lib.validation;

public record FailureDetail(FailureType type, ErrorCode errorCode, String title, String message) {
}
