package ld.e2e.support;

import ld.bootstrap.request.CreateProductRequest;
import ld.bootstrap.request.ProductCategoryRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ProductFixtures {

    private final Api api;

    public ProductFixtures(Api api) {
        this.api = api;
    }

    public UUID create(String name, ProductCategoryRequest category, List<String> colors, BigDecimal price) {
        String id = api.admin()
                .body(new CreateProductRequest(name, category, colors, price))
                .post("/products")
                .then().statusCode(201)
                .extract().path("productId");
        return UUID.fromString(id);
    }

    public UUID bonnet() {
        return create("Bonnet en laine", ProductCategoryRequest.CLOTHING,
                List.of("rouge", "bleu"), new BigDecimal("24.90"));
    }
}