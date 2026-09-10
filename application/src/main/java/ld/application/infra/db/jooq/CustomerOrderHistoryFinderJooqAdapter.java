package ld.application.infra.db.jooq;

import ld.domain.features.order.accept.CustomerOrderHistoryFinder;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.Orders.ORDERS;

@Repository
public class CustomerOrderHistoryFinderJooqAdapter implements CustomerOrderHistoryFinder {

    private final DSLContext dsl;

    public CustomerOrderHistoryFinderJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean hasEffectiveOrder(UUID customerIdentitySubject) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(ORDERS)
                        .join(CUSTOMERS).on(ORDERS.CUSTOMER_IDENTITY_SUBJECT.eq(CUSTOMERS.IDENTITY_SUBJECT))
                        .where(CUSTOMERS.IDENTITY_SUBJECT.eq(customerIdentitySubject))
                        .and(ORDERS.STATUS_TYPE.notIn("REJECTED", "PENDING"))
        );
    }
}
