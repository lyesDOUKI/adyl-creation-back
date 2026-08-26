package ld.application.read;

import ld.application.response.GetProductsResponse;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static ld.application.jooq.tables.OrderDetails.ORDER_DETAILS;
import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.jooq.impl.DSL.*;

@Service
public class GetProductServiceImp implements GetProductService {

    private final DSLContext dsl;
    private final ProductPhotoUrlResolver photoUrlResolver;

    @Autowired
    public GetProductServiceImp(DSLContext dsl, ProductPhotoUrlResolver photoUrlResolver) {
        this.dsl = dsl;
        this.photoUrlResolver = photoUrlResolver;
    }

    @Override
    public List<GetProductsResponse> findAll() {

        Field<List<String>> colorsField = multiset(
                select(PRODUCT_COLORS.COLOR)
                        .from(PRODUCT_COLORS)
                        .where(PRODUCT_COLORS.PRODUCT_ID.eq(PRODUCTS.ID))
        ).as("colors")
                .convertFrom(result -> result.map(Record1::value1));

        Field<List<String>> photoStorageKeysField = multiset(
                select(PRODUCT_PHOTOS.STORAGE_KEY)
                        .from(PRODUCT_PHOTOS)
                        .where(PRODUCT_PHOTOS.PRODUCT_ID.eq(PRODUCTS.ID))
                        .orderBy(PRODUCT_PHOTOS.POSITION)
        ).as("photoStorageKeys")
                .convertFrom(result -> result.map(Record1::value1));

        Field<Integer> numberOfOrdersField = field(
                select(countDistinct(ORDER_DETAILS.ORDER_ID))
                        .from(ORDER_DETAILS)
                        .where(ORDER_DETAILS.PRODUCT_ID.eq(PRODUCTS.ID))
        ).as("numberOfOrders");

        return dsl
                .select(
                        PRODUCTS.ID,
                        PRODUCTS.NAME,
                        PRODUCTS.UNIT_PRICE,
                        colorsField,
                        photoStorageKeysField,
                        numberOfOrdersField
                )
                .from(PRODUCTS)
                .fetch(productRow ->
                        toResponse(productRow, colorsField, photoStorageKeysField, numberOfOrdersField));
    }

    private GetProductsResponse toResponse(
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

        return new GetProductsResponse(
                productId,
                productRow.get(PRODUCTS.NAME),
                unitPrice,
                colors,
                photosUri,
                numberOfOrders
        );
    }
}