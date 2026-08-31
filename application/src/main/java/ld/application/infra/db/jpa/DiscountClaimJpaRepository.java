package ld.application.infra.db.jpa;

import ld.application.infra.db.entity.DiscountClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountClaimJpaRepository
        extends JpaRepository<DiscountClaimEntity, DiscountClaimEntity.DiscountClaimId> {
}
