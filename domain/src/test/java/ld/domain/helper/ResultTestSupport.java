package ld.domain.helper;

import ld.lib.validation.FailureDetail;
import ld.lib.validation.FailureType;
import ld.lib.validation.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ResultTestSupport {

    public static <T> T assertSuccess(Result<T> result) {
        return result.resolve(
                value -> value,
                failure -> fail(
                        "Expected success but got failure: ["
                                + failure.type()
                                + "] "
                                + failure.title()
                                + " - "
                                + failure.detail()
                )
        );
    }

    public static void assertFailure(Result<?> result) {
        assertThat(result.isFailure())
                .withFailMessage("Expected failure but got success")
                .isTrue();
    }

    public static <T> FailureDetail assertFailure(Result<T> result, FailureType expectedType) {
        assertThat(result.isFailure()).isTrue();

        FailureDetail detail = result.resolve(
                success -> { throw new AssertionError("Expected failure but got success: " + success); },
                failureDetail -> failureDetail
        );

        assertThat(detail.type()).isEqualTo(expectedType);
        return detail;
    }
}