package ld.lib.validation;

import java.util.function.BiFunction;
import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String title, String detail) implements Result<T> {}

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static Result<Void> ok() {
        return new Success<>(null);
    }

    static <T> Result<T> failure(String title, String detail) {
        return new Failure<>(title, detail);
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() { return this instanceof Failure<T>; }

    default <R> Result<R> map(Function<T, R> mapper) {
        return switch (this) {
            case Success<T> s -> Result.success(mapper.apply(s.value()));
            case Failure<T> f -> Result.failure(f.title(), f.detail());
        };
    }

    default <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return switch (this) {
            case Success<T> s -> mapper.apply(s.value());
            case Failure<T> f -> Result.failure(f.title(), f.detail());
        };
    }

    default <R> R resolve(Function<T, R> onSuccess, BiFunction<String, String, R> onFailure) {
        return switch (this) {
            case Success<T> s -> onSuccess.apply(s.value());
            case Failure<T> f -> onFailure.apply(f.title(), f.detail());
        };
    }
}