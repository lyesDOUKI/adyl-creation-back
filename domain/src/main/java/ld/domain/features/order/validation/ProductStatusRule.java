package ld.domain.features.order.validation;

import ld.domain.features.order.OrderCreateError;
import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.model.ProductStatus;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.util.List;
import java.util.UUID;

public class ProductStatusRule implements BusinessRule<CreateOrderContextValidation, OrderCreateError> {

    @Override
    public Result<Void, OrderCreateError> apply(CreateOrderContextValidation context) {
        List<UUID> unavailableProducts = context.products().stream()
                .filter(product -> ProductStatus.UNAVAILABLE.equals(product.productStatus()))
                .map(ProductSnapshot::productId)
                .toList();

        return unavailableProducts.isEmpty()
                ? Result.ok()
                : Result.failure(new OrderCreateError.UnavailableProducts(unavailableProducts));
    }
}