package ld.application.infra.db.jooq;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetProductQueryRepository {

    Page<ProductQuery> findAll(Pageable pageable);

    Optional<ProductQuery> findById(UUID productId);
}