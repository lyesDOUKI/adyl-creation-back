package ld.domain.features.order.deliver;

import ld.domain.features.order.model.OrderSnapshot;
import ld.standard.lib.validation.Result;

public interface DeliverOrderUseCase {
    Result<OrderSnapshot> execute(DeliverOrderCommand deliverOrderCommand);
}
