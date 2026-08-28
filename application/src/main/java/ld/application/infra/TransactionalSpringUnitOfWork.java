package ld.application.infra;

import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.Result;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class TransactionalSpringUnitOfWork implements UnitOfWork {

    private final TransactionTemplate transactionTemplate;

    public TransactionalSpringUnitOfWork(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> Result<T> execute(Supplier<Result<T>> operation) {
        return transactionTemplate.execute(status -> {
            Result<T> result = operation.get();
            if (result.isFailure()) {
                status.setRollbackOnly();
            }
            return result;
        });
    }
}
