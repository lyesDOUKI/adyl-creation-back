package ld.application.infra.db.read.jooq;

import ld.application.infra.db.read.GetProductQueryRepository;
import ld.application.infra.db.read.ProductQuery;
import ld.application.jooq.JooqSortUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.jooq.impl.DSL.*;

@Repository
public class JooqGetProductQueryRepository
        implements GetProductQueryRepository {

    private final DSLContext dsl;

    public JooqGetProductQueryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<ProductQuery> findById(UUID productId) {

        Field<List<String>> colors = colorsField();
        Field<List<String>> photoStorageKeys = photoStorageKeysField();
        Field<Integer> numberOfOrders = numberOfOrdersField();

        return dsl
                .select(
                        PRODUCTS.ID,
                        PRODUCTS.NAME,
                        PRODUCTS.UNIT_PRICE,
                        colors,
                        photoStorageKeys,
                        numberOfOrders
                )
                .from(PRODUCTS)
                .where(PRODUCTS.ID.eq(productId))
                .fetchOptional(record -> new ProductQuery(
                        record.get(PRODUCTS.ID),
                        record.get(PRODUCTS.NAME),
                        record.get(PRODUCTS.UNIT_PRICE),
                        record.get(colors),
                        record.get(photoStorageKeys),
                        record.get(numberOfOrders)
                ));
    }

    @Override
    public Page<ProductQuery> findAll(Pageable pageable) {

        Field<List<String>> colors = colorsField();
        Field<List<String>> photoStorageKeys = photoStorageKeysField();
        Field<Integer> numberOfOrders = numberOfOrdersField();

        Map<String, Field<?>> sortableFields = Map.of(
                "productId", PRODUCTS.ID,
                "name", PRODUCTS.NAME,
                "price", PRODUCTS.UNIT_PRICE,
                "numberOfOrders", numberOfOrders
        );

        List<SortField<?>> orderFields =
                JooqSortUtils.toOrderFields(
                        pageable.getSort(),
                        sortableFields,
                        PRODUCTS.ID.asc()
                );

        int totalElements = dsl.fetchCount(PRODUCTS);

        List<ProductQuery> content = dsl
                .select(
                        PRODUCTS.ID,
                        PRODUCTS.NAME,
                        PRODUCTS.UNIT_PRICE,
                        colors,
                        photoStorageKeys,
                        numberOfOrders
                )
                .from(PRODUCTS)
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(record -> new ProductQuery(
                        record.get(PRODUCTS.ID),
                        record.get(PRODUCTS.NAME),
                        record.get(PRODUCTS.UNIT_PRICE),
                        record.get(colors),
                        record.get(photoStorageKeys),
                        record.get(numberOfOrders)
                ));

        return new PageImpl<>(
                content,
                pageable,
                totalElements
        );
    }

    private Field<Integer> numberOfOrdersField() {
        return field(
                select(countDistinct(ORDER_DETAILS.ORDER_ID))
                        .from(ORDER_DETAILS)
                        .where(ORDER_DETAILS.PRODUCT_ID.eq(PRODUCTS.ID))
        ).as("numberOfOrders");
    }

    private Field<List<String>> photoStorageKeysField() {
        return multiset(
                select(PRODUCT_PHOTOS.STORAGE_KEY)
                        .from(PRODUCT_PHOTOS)
                        .where(PRODUCT_PHOTOS.PRODUCT_ID.eq(PRODUCTS.ID))
                        .orderBy(PRODUCT_PHOTOS.POSITION)
        ).as("photoStorageKeys")
                .convertFrom(result -> result.map(Record1::value1));
    }

    private Field<List<String>> colorsField() {
        return multiset(
                select(PRODUCT_COLORS.COLOR)
                        .from(PRODUCT_COLORS)
                        .where(PRODUCT_COLORS.PRODUCT_ID.eq(PRODUCTS.ID))
        ).as("colors")
                .convertFrom(result -> result.map(Record1::value1));
    }
}