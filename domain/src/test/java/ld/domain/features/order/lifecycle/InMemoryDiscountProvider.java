package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;
import ld.domain.valueObjects.Percentage;

import java.util.Optional;

public class InMemoryDiscountProvider implements DiscountProvider {
    @Override
    public Optional<Percentage> provide(DiscountType discountType) {
        return Optional.of(Percentage.of(10));
    }
}
