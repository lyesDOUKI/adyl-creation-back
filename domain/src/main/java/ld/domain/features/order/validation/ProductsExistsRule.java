package ld.domain.features.order.validation;

import ld.domain.features.order.CreateOrderCommand;
import ld.domain.features.order.CreateOrderRepository;
import ld.lib.validation.BusinessRule;
import ld.lib.validation.Result;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductsExistsRule implements BusinessRule<CreateOrderCommand> {

    private final CreateOrderRepository  createOrderRepository;
    public ProductsExistsRule(CreateOrderRepository createOrderRepository) {
        this.createOrderRepository = createOrderRepository;
    }

    @Override
    public Result<Void> apply(CreateOrderCommand context) {

        Set<UUID> requestedProducts = context.createOrderItems().stream()
                .map(CreateOrderCommand.CreateOrderItem::productId)
                .collect(Collectors.toSet());

        Set<UUID> existingIds = createOrderRepository.findExistingProductIds(requestedProducts);

        Set<UUID> missingIds = new HashSet<>(requestedProducts);
        missingIds.removeAll(existingIds);

        if (!missingIds.isEmpty()) {
            return Result.resourceNotFound("Produits introuvables",
                    "des produits de la commandes sont introuvables (identifiants : " + missingIds + ")");
        }
        return Result.ok();
    }
}
