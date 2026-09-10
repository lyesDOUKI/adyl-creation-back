package ld.application.infra.db.jpa.adapter;

import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.model.DiscountType;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static ld.application.jooq.tables.DiscountClaims.DISCOUNT_CLAIMS;

@Repository
public class DiscountClaimerJooqAdapter implements DiscountClaimer {

    private final DSLContext dsl;

    public DiscountClaimerJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean tryAddClaim(DiscountType type, UUID customerIdentitySubject) {
        int rowsInserted = dsl.insertInto(DISCOUNT_CLAIMS)
                .columns(DISCOUNT_CLAIMS.TYPE, DISCOUNT_CLAIMS.CUSTOMER_IDENTITY_SUBJECT)
                .values(type.name(), customerIdentitySubject)
                .onConflictDoNothing()
                .execute();

        return rowsInserted > 0;
    }
}