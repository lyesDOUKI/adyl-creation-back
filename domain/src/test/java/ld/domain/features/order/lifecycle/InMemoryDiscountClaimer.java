package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDiscountClaimer implements DiscountClaimer {

    private final Set<String> claims = ConcurrentHashMap.newKeySet();

    @Override
    public boolean tryAddClaim(DiscountType type, String claimKey) {
        return claims.add(type.name() + "::" + claimKey.toLowerCase());
    }

    public void clear() {
        this.claims.clear();
    }
}
