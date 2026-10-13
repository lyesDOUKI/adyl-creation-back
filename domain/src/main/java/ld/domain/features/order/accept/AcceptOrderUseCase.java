package ld.domain.features.order.accept;

import ld.domain.features.order.lifecycle.OrderLifecycleError;
import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.validation.Result;

public interface AcceptOrderUseCase {
    Result<OrderSnapshot, OrderLifecycleError> execute(AcceptOrderCommand acceptOrderCommand);
}
