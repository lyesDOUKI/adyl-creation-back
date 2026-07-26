package ld.domain.helper;

import ld.lib.validation.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ResultTestSupport {

    public static <T> T assertSuccess(Result<T> result) {
        return result.resolve(
                value -> value,
                (title, detail) -> fail("Expected success but got failure: [" + title + "] " + detail)
        );
    }

    public static void assertFailure(Result<?> result) {
        assertThat(result.isFailure())
                .withFailMessage("Expected failure but got success")
                .isTrue();
    }
}
