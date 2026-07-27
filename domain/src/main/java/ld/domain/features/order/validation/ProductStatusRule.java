package ld.domain.features.order.validation;

import ld.domain.features.product.model.ProductSnapshot;
import ld.domain.features.product.model.ProductStatus;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.util.List;

public class ProductStatusRule implements BusinessRule<CreateOrderContextValidation> {

    @Override
    public Result<Void> apply(CreateOrderContextValidation context) {
        List<String> unavailableProductNames = context.products().stream()
                .filter(product -> ProductStatus.UNAVAILABLE.equals(product.productStatus()))
                .map(ProductSnapshot::name)
                .toList();

        if (!unavailableProductNames.isEmpty()) {
            return Result.businessFailure(
                    OrderErrorCode.PRODUCT_NOT_AVAILABLE,
                    "Produits indisponibles",
                    "Les produits suivants sont indisponibles : " + String.join(", ", unavailableProductNames)
            );
        }

        return Result.ok();
    }
}