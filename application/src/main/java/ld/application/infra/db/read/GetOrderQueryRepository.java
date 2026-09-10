package ld.application.infra.db.read;

import ld.application.infra.db.jooq.OrderQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetOrderQueryRepository {
    Page<OrderQuery> findAll(Pageable pageable, UUID customerId);
    Optional<OrderQuery> findById(UUID orderId, UUID customerId);
}
