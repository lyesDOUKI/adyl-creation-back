package ld.domain.features.order.validation;

import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.model.ProductStatus;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.util.List;
import java.util.UUID;

public class ProductStatusRule implements BusinessRule<CreateOrderContextValidation> {

    @Override
    public Result<Void> apply(CreateOrderContextValidation context) {
        List<UUID> unavailableProducts = context.products().stream()
                .filter(product -> ProductStatus.UNAVAILABLE.equals(product.productStatus()))
                .map(ProductSnapshot::productId)
                .toList();

        if (!unavailableProducts.isEmpty()) {
            return Result.businessFailure(
                    OrderErrorCode.PRODUCT_NOT_AVAILABLE,
                    "Produits indisponibles",
                    String.format("Les produits suivants sont indisponibles (identifiants : %s)", unavailableProducts)
            );
        }

        return Result.ok();
    }
}