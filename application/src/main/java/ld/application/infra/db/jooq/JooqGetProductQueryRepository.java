package ld.application.infra.db.jooq;

import ld.application.infra.db.jooq.utils.JooqSortUtils;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.jooq.impl.DSL.*;

@Repository
public class JooqGetProductQueryRepository implements GetProductQueryRepository {

    private final DSLContext dsl;

    public JooqGetProductQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    private static final Field<List<String>> COLORS = multiset(
            select(PRODUCT_COLORS.COLOR)
                    .from(PRODUCT_COLORS)
                    .where(PRODUCT_COLORS.PRODUCT_ID.eq(PRODUCTS.ID))
    ).as("colors").convertFrom(result -> result.map(Record1::value1));

    private static final Field<List<String>> PHOTO_STORAGE_KEYS = multiset(
            select(PRODUCT_PHOTOS.STORAGE_KEY)
                    .from(PRODUCT_PHOTOS)
                    .where(PRODUCT_PHOTOS.PRODUCT_ID.eq(PRODUCTS.ID))
                    .orderBy(PRODUCT_PHOTOS.POSITION)
    ).as("photoStorageKeys").convertFrom(result -> result.map(Record1::value1));

    private static final Field<Integer> NUMBER_OF_ORDERS = field(
            select(countDistinct(ORDER_DETAILS.ORDER_ID))
                    .from(ORDER_DETAILS)
                    .where(ORDER_DETAILS.PRODUCT_ID.eq(PRODUCTS.ID))
    ).as("numberOfOrders");


    private static final Field<Integer> TOTAL_COUNT = count()
            .over()
            .as("totalCount");


    private static final List<Field<?>> PRODUCT_FIELDS = List.of(
            PRODUCTS.ID,
            PRODUCTS.NAME,
            PRODUCTS.UNIT_PRICE,
            COLORS,
            PHOTO_STORAGE_KEYS,
            NUMBER_OF_ORDERS
    );

    @Override
    public Optional<ProductQuery> findById(UUID productId) {
        return dsl.select(PRODUCT_FIELDS)
                .from(PRODUCTS)
                .where(PRODUCTS.ID.eq(productId))
                .fetchOptional(this::mapToProductQuery);
    }

    @Override
    public Page<ProductQuery> findAll(Pageable pageable) {
        Map<String, Field<?>> sortableFields = Map.of(
                "productId", PRODUCTS.ID,
                "name", PRODUCTS.NAME,
                "price", PRODUCTS.UNIT_PRICE,
                "numberOfOrders", NUMBER_OF_ORDERS
        );
        List<SortField<?>> orderFields = JooqSortUtils.toOrderFields(
                pageable.getSort(),
                sortableFields,
                PRODUCTS.ID.asc()
        );

        List<Field<?>> selectedFields = new ArrayList<>(PRODUCT_FIELDS);
        selectedFields.add(TOTAL_COUNT);

        Result<Record> records = dsl.select(selectedFields)
                .from(PRODUCTS)
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        int totalElements = records.isEmpty()
                ? 0
                : records.getFirst().get(TOTAL_COUNT);

        List<ProductQuery> content = records.stream()
                .map(this::mapToProductQuery)
                .toList();

        return new PageImpl<>(content, pageable, totalElements);
    }

    private ProductQuery mapToProductQuery(Record record) {
        return new ProductQuery(
                record.get(PRODUCTS.ID),
                record.get(PRODUCTS.NAME),
                record.get(PRODUCTS.UNIT_PRICE),
                record.get(COLORS),
                record.get(PHOTO_STORAGE_KEYS),
                record.get(NUMBER_OF_ORDERS)
        );
    }
}