package ld.application.infra.db.jooq;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderQuery(
        UUID orderId,
        UUID customerId,
        String orderReference,
        String customerMessage,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal total,
        String statusType,
        List<OrderLineQuery> lines
) {}
