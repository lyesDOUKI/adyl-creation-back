package ld.domain.features.order.cancel;

import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.validation.Result;

public interface CancelOrderUseCase {
    Result<OrderSnapshot, CancelOrderError> execute(CancelOrderCommand cancelOrderCommand);
}
