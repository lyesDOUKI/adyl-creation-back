package ld.domain.helper;

import ld.standard.lib.validation.ErrorCode;
import ld.standard.lib.validation.FailureDetail;
import ld.standard.lib.validation.FailureType;
import ld.standard.lib.validation.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ResultTestSupport {

    public static <T> void assertSuccess(Result<T> result) {
        result.resolve(
                value -> value,
                failure -> fail(
                        "Expected success but got failure: ["
                                + failure.failureType()
                                + "] "
                                + failure.title()
                                + " - "
                                + failure.message()
                )
        );
    }

    public static void assertFailure(Result<?> result) {
        assertThat(result.isFailure())
                .withFailMessage("Expected failure but got success")
                .isTrue();
    }

    public static <T> void assertFailure(Result<T> result, FailureType expectedType, ErrorCode expectedCode) {
        assertThat(result.isFailure()).isTrue();

        FailureDetail detail = result.resolve(
                success -> { throw new AssertionError("Expected failure but got success: " + success); },
                failureDetail -> failureDetail
        );

        assertThat(detail.failureType()).isEqualTo(expectedType);
        assertThat(detail.errorCode()).isEqualTo(expectedCode);
    }
}