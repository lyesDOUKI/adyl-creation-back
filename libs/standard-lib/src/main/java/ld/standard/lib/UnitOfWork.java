package ld.standard.lib;

import ld.standard.lib.validation.Result;

import java.util.function.Supplier;

public interface UnitOfWork {
    <T, E> Result<T, E> executeInTransaction(Supplier<Result<T, E>> operation);
}
