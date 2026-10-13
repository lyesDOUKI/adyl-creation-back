package ld.standard.lib.validation;

public interface BusinessRule<T, E> {
    Result<Void, E> apply(T context);
}