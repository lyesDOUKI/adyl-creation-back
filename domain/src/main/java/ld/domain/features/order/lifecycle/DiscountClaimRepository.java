package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;

public interface DiscountClaimRepository {
    boolean tryClaim(DiscountType type, String claimKey);
}