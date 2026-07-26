package ld.domain.features.order.validation;

import ld.domain.features.order.CreateOrderCommand;
import ld.domain.features.order.CreateOrderRepository;
import ld.lib.validation.BusinessRule;
import ld.lib.validation.Result;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductExistsRule implements BusinessRule<CreateOrderCommand> {

    private final CreateOrderRepository  createOrderRepository;
    public ProductExistsRule(CreateOrderRepository createOrderRepository) {
        this.createOrderRepository = createOrderRepository;
    }

    @Override
    public Result<Void> apply(CreateOrderCommand context) {

        Set<UUID> requestedIds = context.createOrderItems().stream()
                .map(CreateOrderCommand.CreateOrderItem::productId)
                .collect(Collectors.toSet());

        Set<UUID> existingIds = createOrderRepository.findExistingProducts(requestedIds);

        Set<UUID> missingIds = new HashSet<>(requestedIds);
        missingIds.removeAll(existingIds);

        if (!missingIds.isEmpty()) {
            return Result.failure("Produits introuvables", "listes : " + missingIds);
        }
        return Result.ok();
    }
}
