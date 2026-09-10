package ld.application.infra.db.jooq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderQuery(
        UUID orderId,
        UUID customerId,
        String orderReference,
        String customerMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal total,
        String statusType,
        List<OrderLineQuery> lines
) {}
