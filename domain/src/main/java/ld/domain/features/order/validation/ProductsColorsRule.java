package ld.domain.features.order.validation;

import ld.domain.features.order.OrderCreateError;
import ld.domain.features.product.model.ProductColor;
import ld.domain.features.product.model.ProductSnapshot;
import ld.standard.lib.validation.BusinessRule;
import ld.standard.lib.validation.Result;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductsColorsRule implements BusinessRule<CreateOrderContextValidation, OrderCreateError> {

    @Override
    public Result<Void, OrderCreateError> apply(CreateOrderContextValidation context) {
        Map<UUID, List<ProductColor>> colorsByProductId = context.products().stream()
                .collect(Collectors.toMap(
                        ProductSnapshot::productId,
                        product -> product.colors() == null ? List.of() : product.colors()
                ));

        for (var item : context.createOrderCommand().createOrderItems()) {
            List<ProductColor> availableColors = colorsByProductId.get(item.productId());

            if (availableColors.isEmpty()) {
                continue;
            }
            if (!availableColors.contains(new ProductColor(item.color()))) {
                return Result.failure(new OrderCreateError.ColorNotAvailable(
                        item.productId(), item.color(), availableColors));
            }
        }

        return Result.ok();
    }
}