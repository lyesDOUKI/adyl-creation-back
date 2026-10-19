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

    public static <T, E extends DomainError> void assertFailure(Result<T, E> result, E expectedError) {
        assertThat(extractError(result)).isEqualTo(expectedError);
    }

    public static <T, E extends DomainError, C extends E> C assertFailureOfType(
            Result<T, E> result, Class<C> expectedCase) {
        E error = extractError(result);
        assertThat(error).isInstanceOf(expectedCase);
        return expectedCase.cast(error);
    }

    public static <T, E extends DomainError> T extractValue(Result<T, E> result) {
        return result.fold(
                value -> value,
                error -> {
                    throw new AssertionError("Expected success but got failure: " + error);
                });
    }

    public static <T, E extends DomainError> E extractError(Result<T, E> result) {
        return result.fold(
                value -> {
                    throw new AssertionError("Expected failure but got success: " + value);
                },
                error -> error);
    }
}