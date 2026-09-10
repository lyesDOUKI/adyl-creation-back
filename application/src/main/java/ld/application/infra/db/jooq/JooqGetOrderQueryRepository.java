package ld.application.infra.db.jooq;

import ld.application.infra.db.jooq.utils.JooqSortUtils;
import ld.application.infra.db.read.GetOrderQueryRepository;
import ld.application.jooq.tables.records.OrdersRecord;
import ld.domain.features.product.model.ProductCategory;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.Orders.ORDERS;
import static ld.application.jooq.tables.Products.PRODUCTS;

@Repository
public class JooqGetOrderQueryRepository implements GetOrderQueryRepository {

    private static final Map<String, Field<?>> SORTABLE_FIELDS = Map.of(
            "createdAt", ORDERS.CREATED_AT,
            "updatedAt", ORDERS.UPDATED_AT,
            "total", ORDERS.TOTAL,
            "customerId", ORDERS.CUSTOMER_IDENTITY_SUBJECT
    );

    private final DSLContext dsl;

    public JooqGetOrderQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<OrderQuery> findById(UUID orderId, UUID customerId) {

        var orderRecord = dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(orderId))
                .and(ORDERS.CUSTOMER_IDENTITY_SUBJECT.eq(customerId))
                .fetchOne();

        if (orderRecord == null) {
            return Optional.empty();
        }

        List<OrderLineQuery> lines = fetchLines(List.of(orderId))
                .getOrDefault(orderId, List.of());

        return Optional.of(toOrderQuery(orderRecord, lines));
    }

    @Override
    public Page<OrderQuery> findAll(Pageable pageable, UUID customerId) {

        int totalElements = dsl.fetchCount(ORDERS, ORDERS.CUSTOMER_IDENTITY_SUBJECT.eq(customerId));

        List<SortField<?>> orderFields = JooqSortUtils.toOrderFields(
                pageable.getSort(),
                SORTABLE_FIELDS,
                ORDERS.CREATED_AT.desc()
        );

        var orderRecords = dsl.selectFrom(ORDERS)
                .where(ORDERS.CUSTOMER_IDENTITY_SUBJECT.eq(customerId))
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        List<UUID> orderIds = orderRecords.stream()
                .map(OrdersRecord::getId)
                .toList();

        Map<UUID, List<OrderLineQuery>> linesByOrderId = fetchLines(orderIds);

        List<OrderQuery> content = orderRecords.stream()
                .map(record -> toOrderQuery(
                        record,
                        linesByOrderId.getOrDefault(record.getId(), List.of())
                ))
                .toList();

        return new PageImpl<>(content, pageable, totalElements);
    }

    private Map<UUID, List<OrderLineQuery>> fetchLines(List<UUID> orderIds) {

        if (orderIds.isEmpty()) {
            return Map.of();
        }

        return dsl.select(
                        ORDER_DETAILS.ORDER_ID,
                        ORDER_DETAILS.PRODUCT_ID,
                        PRODUCTS.NAME,
                        PRODUCTS.CATEGORY,
                        ORDER_DETAILS.QUANTITY,
                        ORDER_DETAILS.UNIT_PRICE,
                        ORDER_DETAILS.TOTAL_AMOUNT,
                        ORDER_DETAILS.CHOSEN_COLOR,
                        ORDER_DETAILS.DISCOUNT_RATE
                )
                .from(ORDER_DETAILS)
                .join(PRODUCTS).on(PRODUCTS.ID.eq(ORDER_DETAILS.PRODUCT_ID))
                .where(ORDER_DETAILS.ORDER_ID.in(orderIds))
                .orderBy(ORDER_DETAILS.ID)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.get(ORDER_DETAILS.ORDER_ID),
                        LinkedHashMap::new,
                        Collectors.mapping(this::toOrderLineQuery, Collectors.toList())
                ));
    }

    private OrderLineQuery toOrderLineQuery(Record r) {
        return new OrderLineQuery(
                r.get(ORDER_DETAILS.PRODUCT_ID),
                r.get(PRODUCTS.NAME),
                ProductCategory.valueOf(r.get(PRODUCTS.CATEGORY)),
                r.get(ORDER_DETAILS.QUANTITY),
                r.get(ORDER_DETAILS.UNIT_PRICE),
                r.get(ORDER_DETAILS.TOTAL_AMOUNT),
                r.get(ORDER_DETAILS.CHOSEN_COLOR),
                r.get(ORDER_DETAILS.DISCOUNT_RATE)
        );
    }

    private OrderQuery toOrderQuery(OrdersRecord record, List<OrderLineQuery> lines) {
        return new OrderQuery(
                record.getId(),
                record.getCustomerIdentitySubject(),
                record.getOrderReference(),
                record.getCustomerMessage(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getTotal(),
                record.getStatusType(),
                lines
        );
    }
}