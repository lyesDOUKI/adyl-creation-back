package ld.lib.validation;

import java.util.function.Function;

public sealed interface Result<T>
        permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {
    }

    record Failure<T>(FailureDetail detail) implements Result<T> {
    }

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static Result<Void> ok() {
        return new Success<>(null);
    }

    static <T> Result<T> businessFailure(
            String title,
            String detail
    ) {
        return failure(
                FailureType.BUSINESS_RULE,
                title,
                detail
        );
    }

    static <T> Result<T> resourceNotFound(
            String title,
            String detail
    ) {
        return failure(
                FailureType.RESOURCE_NOT_FOUND,
                title,
                detail
        );
    }

    static <T> Result<T> failure(
            FailureType type,
            String title,
            String detail
    ) {
        return new Failure<>(
                new FailureDetail(type, title, detail)
        );
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    default <R> Result<R> map(Function<T, R> mapper) {
        return switch (this) {
            case Success<T> success ->
                    Result.success(mapper.apply(success.value()));

            case Failure<T> failure ->
                    Result.failure(
                            failure.detail().type(),
                            failure.detail().title(),
                            failure.detail().detail()
                    );
        };
    }

    default <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return switch (this) {
            case Success<T> success ->
                    mapper.apply(success.value());

            case Failure<T> failure ->
                    Result.failure(
                            failure.detail().type(),
                            failure.detail().title(),
                            failure.detail().detail()
                    );
        };
    }

    default <R> R resolve(
            Function<T, R> onSuccess,
            Function<FailureDetail, R> onFailure
    ) {
        return switch (this) {
            case Success<T> success ->
                    onSuccess.apply(success.value());

            case Failure<T> failure ->
                    onFailure.apply(failure.detail());
        };
    }
}