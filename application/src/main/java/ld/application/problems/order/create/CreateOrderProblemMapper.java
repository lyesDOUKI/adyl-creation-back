package ld.application.problems.order.create;

import ld.domain.features.order.OrderCreateError;
import ld.domain.features.product.model.ProductColor;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.ProblemSpecs;

import java.util.HashSet;
import java.util.stream.Collectors;

public class CreateOrderProblemMapper implements ProblemMapper<OrderCreateError> {

    @Override
    public ProblemDetailResponse toProblem(OrderCreateError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private CreateOrderProblem toApi(OrderCreateError error) {
        return switch (error) {
            case OrderCreateError.ColorNotAvailable e -> new CreateOrderProblem.colorNotAvailable(
                    e.productId(),
                    e.color(),
                    e.availableColors().stream().map(ProductColor::value).collect(Collectors.toUnmodifiableSet()));

            case OrderCreateError.UnavailableProducts e -> new CreateOrderProblem.unavailableProducts(
                    new HashSet<>(e.productIds()));

            case OrderCreateError.UnknownProducts e -> new CreateOrderProblem.unknownProducts(
                    e.productIds());
        };
    }
}
