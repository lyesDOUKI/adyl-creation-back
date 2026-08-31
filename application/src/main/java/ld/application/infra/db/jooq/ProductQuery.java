package ld.application.infra.db.jooq;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductQuery(
        UUID productId,
        String name,
        BigDecimal unitPrice,
        List<String> colors,
        List<String> photoStorageKeys,
        int numberOfOrders
) {
}
