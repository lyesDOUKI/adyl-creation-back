package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;
import ld.domain.valueObjects.Percentage;

import java.util.Optional;

public interface DiscountProvider {
    Optional<Percentage> provide(DiscountType discountType);
}
