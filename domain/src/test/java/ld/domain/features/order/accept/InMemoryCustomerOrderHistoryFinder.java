package ld.domain.features.order.accept;

import java.util.HashSet;
import java.util.Set;

public class InMemoryCustomerOrderHistoryFinder implements CustomerOrderHistoryFinder {

    private final Set<String> customersWithEffectiveOrder = new HashSet<>();

    @Override
    public boolean hasEffectiveOrder(String customerEmail) {
        return customersWithEffectiveOrder.contains(customerEmail);
    }

    public void markEffectiveOrder(String customerEmail) {
        customersWithEffectiveOrder.add(customerEmail);
    }

    public void clear() {
        customersWithEffectiveOrder.clear();
    }
}