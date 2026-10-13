package ld.standard.lib.helper.test;

import ld.standard.lib.validation.*;

import static org.assertj.core.api.Assertions.assertThat;

public final class ResultTestSupport {

    private ResultTestSupport() {}

    public static <T, E extends DomainError> void assertSuccess(Result<T, E> result) {
        extractValue(result);
    }

    public static void assertFailure(Result<?, ?> result) {
        assertThat(result.isFailure())
                .withFailMessage("Expected failure but got success")
                .isTrue();
    }

    public static <T, E extends DomainError> void assertFailure(
            Result<T, E> result, FailureType expectedType, ErrorCode expectedCode) {

        FailureDetail detail = extractFailureDetail(result);

        assertThat(detail.failureType()).isEqualTo(expectedType);
        assertThat(detail.errorCode()).isEqualTo(expectedCode);
    }

    public static <T, E extends DomainError> void assertFailure(Result<T, E> result, E expectedError) {
        assertThat(extractError(result)).isEqualTo(expectedError);
    }

    public static <T, E extends DomainError> void assertFailureOfType(
            Result<T, E> result, Class<? extends E> expectedCase) {
        assertThat(extractError(result)).isInstanceOf(expectedCase);
    }

    public static <T, E extends DomainError> T extractValue(Result<T, E> result) {
        return result.fold(
                value -> value,
                error -> {
                    throw new AssertionError("Expected success but got failure: " + describe(error));
                });
    }

    public static <T, E extends DomainError> E extractError(Result<T, E> result) {
        return result.fold(
                value -> {
                    throw new AssertionError("Expected failure but got success: " + value);
                },
                error -> error);
    }

    private static <T, E extends DomainError> FailureDetail extractFailureDetail(Result<T, E> result) {
        return extractError(result).detail();
    }

    private static String describe(DomainError error) {
        FailureDetail detail = error.detail();
        return "[" + detail.failureType() + "] " + detail.title() + " - " + detail.message();
    }
}