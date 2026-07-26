package ld.domain.features.order;

import ld.lib.validation.Result;

public interface CreateOrderUseCase {
    Result<Void> execute(CreateOrderCommand createOrderCommand);
}
