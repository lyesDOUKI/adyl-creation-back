package ld.domain.features.order.validation;

import ld.domain.features.product.model.ProductSnapshot;
import ld.lib.validation.BusinessRule;
import ld.lib.validation.Result;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductsColorsRule implements BusinessRule<CreateOrderContextValidation> {

    @Override
    public Result<Void> apply(CreateOrderContextValidation context) {
        Map<UUID, List<String>> productColorsByProductId =
                context.products().stream()
                        .collect(Collectors.toMap(
                                ProductSnapshot::productId,
                                productSnapshot -> {
                                    if (productSnapshot.colors() == null) {
                                        return Collections.emptyList();
                                    }
                                    return productSnapshot.colors()
                                            .stream().map(String::toUpperCase).toList();
                                }
                        ));
        for (var item : context.createOrderCommand().createOrderItems()) {
            List<String> availableColors = productColorsByProductId.get(item.productId());
            if (availableColors.isEmpty()) {
                return Result.ok();
            }
            if (!availableColors.contains(item.color().toUpperCase())) {
                return Result.businessFailure(
                        "Couleur demandée incohérente",
                        String.format(
                                "La couleur '%s' n'est pas disponible pour le produit identifiant : %s. Couleurs disponibles : %s",
                                item.color(),
                                item.productId(),
                                availableColors
                        )
                );
            }
        }

        return Result.ok();
    }
}
