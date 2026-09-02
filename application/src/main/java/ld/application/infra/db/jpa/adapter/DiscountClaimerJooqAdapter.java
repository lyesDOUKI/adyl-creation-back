package ld.application.infra.db.jpa.adapter;

import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.model.DiscountType;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ld.application.jooq.tables.DiscountClaims.DISCOUNT_CLAIMS;

@Repository
public class DiscountClaimerJooqAdapter implements DiscountClaimer {

    private final DSLContext dsl;

    public DiscountClaimerJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean tryAddClaim(DiscountType type, String claimKey) {
        int rowsInserted = dsl.insertInto(DISCOUNT_CLAIMS)
                .columns(DISCOUNT_CLAIMS.TYPE, DISCOUNT_CLAIMS.EMAIL)
                .values(type.name(), claimKey)
                .onConflictDoNothing()
                .execute();

        return rowsInserted > 0;
    }
}