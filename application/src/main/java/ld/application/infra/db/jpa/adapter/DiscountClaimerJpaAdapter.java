package ld.application.infra.db.jpa.adapter;

import ld.application.infra.db.entity.DiscountClaimEntity;
import ld.application.infra.db.jpa.DiscountClaimJpaRepository;
import ld.domain.features.order.lifecycle.DiscountClaimer;
import ld.domain.features.order.model.DiscountType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class DiscountClaimerJpaAdapter implements DiscountClaimer {

    private final DiscountClaimJpaRepository repository;

    public DiscountClaimerJpaAdapter(DiscountClaimJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean tryAddClaim(DiscountType type, String claimKey) {
        try {
            repository.saveAndFlush(new DiscountClaimEntity(type, claimKey));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}