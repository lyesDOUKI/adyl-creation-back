package ld.application.response;

import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetOrderResponse(
        UUID orderId,
        UUID customerId,
        String orderReference,
        String customerMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal total,
        String status,
        List<OrderLineResponse> lines,
        int lineCount,
        BigDecimal totalQuantity
) implements ApiResponseBody {}
