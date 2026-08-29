package ld.standard.lib.helper.test;

import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.util.function.Supplier;

public class InMemoryUnitOfWork implements UnitOfWork {
    @Override
    public <T> Result<T> executeInTransaction(Supplier<Result<T>> operation) {
        return operation.get();
    }
}
