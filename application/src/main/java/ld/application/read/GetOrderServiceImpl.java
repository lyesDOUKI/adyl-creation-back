package ld.application.read;

import ld.application.infra.db.jooq.OrderLineQuery;
import ld.application.infra.db.jooq.OrderQuery;
import ld.application.infra.db.read.GetOrderQueryRepository;
import ld.application.response.GetOrderResponse;
import ld.application.response.OrderLineResponse;
import ld.application.response.ProductCategoryResponse;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.standard.lib.validation.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class GetOrderServiceImpl implements GetOrderService {

    private final GetOrderQueryRepository repository;

    public GetOrderServiceImpl(GetOrderQueryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Result<GetOrderResponse> findById(UUID orderId, UUID customerId) {

        return repository.findById(orderId, customerId)
                .map(this::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.resourceNotFound(
                        OrderErrorCode.ORDER_NOT_FOUND,
                        "Commande introuvable",
                        String.format("La commande %s est introuvable", orderId)
                ));
    }

    @Override
    public Page<GetOrderResponse> findAll(Pageable pageable, UUID customerId) {
        return repository.findAll(pageable, customerId)
                .map(this::toResponse);
    }

    private GetOrderResponse toResponse(OrderQuery order) {

        List<OrderLineResponse> lines = order.lines()
                .stream()
                .map(this::toLineResponse)
                .toList();

        BigDecimal totalQuantity = lines.stream()
                .map(OrderLineResponse::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new GetOrderResponse(
                order.orderId(),
                order.customerId(),
                order.customerMessage(),
                order.createdAt(),
                order.updatedAt(),
                order.total(),
                order.statusType(),
                lines,
                lines.size(),
                totalQuantity
        );
    }

    private OrderLineResponse toLineResponse(OrderLineQuery line) {

        BigDecimal subtotalBeforeDiscount = line.unitPrice().multiply(line.quantity());
        BigDecimal discountAmount = subtotalBeforeDiscount.subtract(line.totalAmount());

        return new OrderLineResponse(
                line.productId(),
                line.productName(),
                ProductCategoryResponse.from(line.productCategory()),
                line.quantity(),
                line.unitPrice(),
                line.chosenColor(),
                subtotalBeforeDiscount,
                line.discountRate(),
                discountAmount,
                line.totalAmount()
        );
    }
}