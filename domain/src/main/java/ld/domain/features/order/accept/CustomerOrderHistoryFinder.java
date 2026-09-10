package ld.domain.features.order.accept;

import java.util.UUID;

public interface CustomerOrderHistoryFinder {
    boolean hasEffectiveOrder(UUID customerIdentitySubject);
}
