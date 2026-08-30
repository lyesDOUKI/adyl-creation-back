package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDiscountClaimRepository implements DiscountClaimRepository {

    private final Set<String> claims = ConcurrentHashMap.newKeySet();

    @Override
    public boolean tryClaim(DiscountType type, String claimKey) {
        return claims.add(type.name() + "::" + claimKey.toLowerCase());
    }
}
