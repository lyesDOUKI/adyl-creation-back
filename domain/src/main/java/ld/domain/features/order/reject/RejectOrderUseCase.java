package ld.domain.features.order.reject;

import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.validation.Result;

public interface RejectOrderUseCase {
    Result<OrderSnapshot> execute(RejectOrderCommand rejectOrderCommand);
}
