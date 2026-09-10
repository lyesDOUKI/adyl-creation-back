package ld.domain.features.order.lifecycle;

import ld.domain.features.order.model.DiscountType;

import java.util.UUID;

public interface DiscountClaimer {
    boolean tryAddClaim(DiscountType type, UUID customerIdentitySubject);
}