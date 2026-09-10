package ld.domain.features.order.accept;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InMemoryCustomerOrderHistoryFinder implements CustomerOrderHistoryFinder {

    private final Set<UUID> customersWithEffectiveOrder = new HashSet<>();

    @Override
    public boolean hasEffectiveOrder(UUID customerIdentitySubject) {
        return customersWithEffectiveOrder.contains(customerIdentitySubject);
    }

    public void markEffectiveOrder(UUID customerIdentity) {
        customersWithEffectiveOrder.add(customerIdentity);
    }

    public void clear() {
        customersWithEffectiveOrder.clear();
    }
}