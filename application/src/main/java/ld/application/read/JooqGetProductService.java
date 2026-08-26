package ld.application.read;

import ld.application.jooq.JooqSortUtils;
import ld.application.response.GetProductResponse;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SortField;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.jooq.impl.DSL.*;


@Service
public class JooqGetProductService implements GetProductService {

    private final DSLContext dsl;
    private final ProductPhotoUrlResolver photoUrlResolver;

    @Autowired
    public JooqGetProductService(DSLContext dsl, ProductPhotoUrlResolver photoUrlResolver) {
        this.dsl = dsl;
        this.photoUrlResolver = photoUrlResolver;
    }

    @Override
    public Page<GetProductResponse> findAll(Pageable pageable) {

        Field<List<String>> colorsField = colorsField();

        Field<List<String>> photoStorageKeysField = photoStorageKeysField();

        Field<Integer> numberOfOrdersField = numberOfOrdersField();

        Map<String, Field<?>> sortableFields = Map.of(
                "productId", PRODUCTS.ID,
                "name", PRODUCTS.NAME,
                "price", PRODUCTS.UNIT_PRICE,
                "numberOfOrders", numberOfOrdersField
        );
        List<SortField<?>> orderFields = JooqSortUtils.toOrderFields(pageable.getSort(), sortableFields, PRODUCTS.ID.asc());

        int totalElements = dsl.fetchCount(PRODUCTS);

        List<GetProductResponse> content = dsl
                .select(
                        PRODUCTS.ID,
                        PRODUCTS.NAME,
                        PRODUCTS.UNIT_PRICE,
                        colorsField,
                        photoStorageKeysField,
                        numberOfOrdersField
                )
                .from(PRODUCTS)
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(productRow ->
                        toResponse(productRow, colorsField, photoStorageKeysField, numberOfOrdersField));

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public Result<GetProductResponse> findById(UUID productId) {
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
                .fetchOptional(productRow ->
                        toResponse(
                                productRow,
                                colors,
                                photoStorageKeys,
                                numberOfOrders
                        )
                )
                .map(Result::success)
                .orElseGet(() -> Result.resourceNotFound(
                        ProductErrorCode.PRODUCTS_NOT_FOUND,
                        "Produit introuvable",
                        String.format("Le produit %s est introuvable", productId)
                ));
    }

    private @NonNull Field<Integer> numberOfOrdersField() {
        return field(
                select(countDistinct(ORDER_DETAILS.ORDER_ID))
                        .from(ORDER_DETAILS)
                        .where(ORDER_DETAILS.PRODUCT_ID.eq(PRODUCTS.ID))
        ).as("numberOfOrders");
    }

    private @NonNull Field<List<String>> photoStorageKeysField() {
        return multiset(
                select(PRODUCT_PHOTOS.STORAGE_KEY)
                        .from(PRODUCT_PHOTOS)
                        .where(PRODUCT_PHOTOS.PRODUCT_ID.eq(PRODUCTS.ID))
                        .orderBy(PRODUCT_PHOTOS.POSITION)
        ).as("photoStorageKeys")
                .convertFrom(result -> result.map(Record1::value1));
    }

    private  @NonNull Field<List<String>> colorsField() {
        return multiset(
                select(PRODUCT_COLORS.COLOR)
                        .from(PRODUCT_COLORS)
                        .where(PRODUCT_COLORS.PRODUCT_ID.eq(PRODUCTS.ID))
        ).as("colors")
                .convertFrom(result -> result.map(Record1::value1));
    }

    private GetProductResponse toResponse(
            org.jooq.Record productRow,
            Field<List<String>> colorsField,
            Field<List<String>> photoStorageKeysField,
            Field<Integer> numberOfOrdersField
    ) {
        UUID productId = productRow.get(PRODUCTS.ID);
        BigDecimal unitPrice = productRow.get(PRODUCTS.UNIT_PRICE);
        List<String> colors = productRow.get(colorsField);
        List<String> photoStorageKeys = productRow.get(photoStorageKeysField);
        int numberOfOrders = productRow.get(numberOfOrdersField);

        List<String> photosUri = photoStorageKeys.stream()
                .map(storageKey -> photoUrlResolver.resolve(productId, storageKey))
                .toList();

        return new GetProductResponse(
                productId,
                productRow.get(PRODUCTS.NAME),
                unitPrice,
                colors,
                photosUri,
                numberOfOrders
        );
    }
}