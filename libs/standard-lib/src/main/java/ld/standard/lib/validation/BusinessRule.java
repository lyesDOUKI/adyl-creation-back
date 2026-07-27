package ld.standard.lib.validation;

public interface BusinessRule<T> {
    Result<Void> apply(T context);
}