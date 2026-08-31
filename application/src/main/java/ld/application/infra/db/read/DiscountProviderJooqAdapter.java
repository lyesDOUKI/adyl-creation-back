package ld.application.infra.db.read;

import ld.domain.features.order.lifecycle.DiscountProvider;
import ld.domain.features.order.model.DiscountType;
import ld.domain.valueObjects.Percentage;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static ld.application.jooq.tables.DiscountRates.DISCOUNT_RATES;

@Repository
public class DiscountProviderJooqAdapter implements DiscountProvider {

    private final DSLContext dslContext;

    public DiscountProviderJooqAdapter(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Optional<Percentage> provide(DiscountType discountType) {
        return dslContext
                .select(DISCOUNT_RATES.RATE)
                .from(DISCOUNT_RATES)
                .where(DISCOUNT_RATES.DISCOUNT_TYPE.eq(discountType.name()))
                .fetchOptional(DISCOUNT_RATES.RATE)
                .map(Percentage::of);
    }
}
