package ld.application.shared.product;

import ld.domain.features.product.model.ProductStatus;
import org.jooq.DSLContext;
import org.springframework.boot.test.context.TestComponent;

import java.math.BigDecimal;
import java.util.UUID;

import static ld.application.jooq.tables.Products.PRODUCTS;

@TestComponent
public class ProductTestFixture {

    private final DSLContext dsl;

    public ProductTestFixture(DSLContext dsl) {
        this.dsl = dsl;
    }

    public ProductRecord createExistingProduct() {
        return createExistingProduct("T-shirt", BigDecimal.valueOf(19.90));
    }

    public ProductRecord createExistingProduct(String name, BigDecimal price) {
        UUID id = UUID.randomUUID();
        dsl.insertInto(PRODUCTS, PRODUCTS.ID, PRODUCTS.NAME, PRODUCTS.UNIT_PRICE, PRODUCTS.STATUS)
                .values(id, name, price, ProductStatus.AVAILABLE.name())
                .execute();
        return new ProductRecord(id, name, price);
    }

    public record ProductRecord(UUID id, String name, BigDecimal price) {
    }
}
