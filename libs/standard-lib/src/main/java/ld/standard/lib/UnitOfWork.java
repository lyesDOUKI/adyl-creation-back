package ld.standard.lib;

import ld.standard.lib.validation.Result;

import java.util.function.Supplier;

public interface UnitOfWork {
    <T> Result<T> execute(Supplier<Result<T>> operation);
}
