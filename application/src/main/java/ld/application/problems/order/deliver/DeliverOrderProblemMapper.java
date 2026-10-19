package ld.application.problems.order.deliver;

import ld.domain.features.order.deliver.DeliverOrderError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.ProblemSpecs;

public class DeliverOrderProblemMapper implements ProblemMapper<DeliverOrderError> {

    @Override
    public ProblemDetailResponse toProblem(DeliverOrderError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private DeliverOrderProblem toApi(DeliverOrderError deliverOrderError) {
        return switch (deliverOrderError) {
            case DeliverOrderError.OrderAlreadyRejected _ -> new DeliverOrderProblem.Rejected();
            case DeliverOrderError.OrderNotFound orderNotFound -> new DeliverOrderProblem.NotFound(orderNotFound.orderId());
            case DeliverOrderError.OrderStillPending _ -> new DeliverOrderProblem.pending();
        };
    }
}
