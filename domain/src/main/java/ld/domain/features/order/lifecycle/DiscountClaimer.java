package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;

public interface DiscountClaimer {
    boolean tryAddClaim(DiscountType type, String claimKey);
}