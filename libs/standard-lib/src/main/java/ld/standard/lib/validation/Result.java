package ld.standard.lib.validation;

import java.util.Objects;
import java.util.function.Function;

public sealed interface Result<T, E> {

    record Success<T, E>(T value) implements Result<T, E> {
    }

    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            Objects.requireNonNull(error, "error must not be null");
        }
    }

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <E> Result<Void, E> ok() {
        return new Success<>(null);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T, E>;
    }

    default <R> Result<R, E> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Success<T, E>(var value) -> success(mapper.apply(value));
            case Failure<T, E>(var error) -> failure(error);
        };
    }

    default <R> Result<R, E> andThen(Function<? super T, ? extends Result<R, E>> nextStep) {
        return switch (this) {
            case Success<T, E>(var value) -> nextStep.apply(value);
            case Failure<T, E>(var error) -> failure(error);
        };
    }

    default <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper) {
        return switch (this) {
            case Success<T, E>(var value) -> success(value);
            case Failure<T, E>(var error) -> failure(mapper.apply(error));
        };
    }

    default <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super E, ? extends R> onFailure
    ) {
        return switch (this) {
            case Success<T, E>(var value) -> onSuccess.apply(value);
            case Failure<T, E>(var error) -> onFailure.apply(error);
        };
    }
}