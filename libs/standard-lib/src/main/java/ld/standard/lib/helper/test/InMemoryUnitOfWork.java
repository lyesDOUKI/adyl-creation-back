package ld.standard.lib.helper.test;

import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;

import java.util.function.Supplier;

public class InMemoryUnitOfWork implements UnitOfWork {

    @Override
    public <T, E> Result<T, E> executeInTransaction(Supplier<Result<T, E>> operation) {
        return operation.get();
    }
}