package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;
import ld.domain.valueObjects.Percentage;

import java.util.Optional;

public class InMemoryDiscountProvider implements DiscountProvider {
    private Percentage percentageOfTen = Percentage.of(10);

    @Override
    public Optional<Percentage> provide(DiscountType discountType) {
        return Optional.ofNullable(percentageOfTen);
    }

    public void clear() {
        this.percentageOfTen = null;
    }
}
