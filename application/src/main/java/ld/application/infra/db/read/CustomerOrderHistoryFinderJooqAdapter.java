package ld.application.infra.db.read;

import ld.domain.features.order.accept.CustomerOrderHistoryFinder;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ld.application.jooq.tables.Customers.CUSTOMERS;
import static ld.application.jooq.tables.Orders.ORDERS;

@Repository
public class CustomerOrderHistoryFinderJooqAdapter implements CustomerOrderHistoryFinder {

    private final DSLContext dsl;

    public CustomerOrderHistoryFinderJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean hasEffectiveOrder(String customerEmail) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(ORDERS)
                        .join(CUSTOMERS).on(ORDERS.CUSTOMER_ID.eq(CUSTOMERS.ID))
                        .where(CUSTOMERS.CUSTOMER_EMAIL.eq(customerEmail))
                        .and(ORDERS.STATUS_TYPE.notIn("REJECTED", "PENDING"))
        );
    }
}
