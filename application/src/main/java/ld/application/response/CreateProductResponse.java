package ld.application.response;

import ld.domain.features.product.model.ProductSnapshot;
import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductResponse(UUID productId, String name, BigDecimal price) implements ApiResponseBody {
    public static CreateProductResponse from(ProductSnapshot productSnapshot) {
        return new CreateProductResponse(productSnapshot.productId(), productSnapshot.name(), productSnapshot.price());
    }
}
