package ld.domain.features.order;

import ld.domain.features.order.model.OrderSnapshot;
import ld.lib.validation.Result;

public interface CreateOrderUseCase {
    Result<OrderSnapshot> execute(CreateOrderCommand createOrderCommand);
}
