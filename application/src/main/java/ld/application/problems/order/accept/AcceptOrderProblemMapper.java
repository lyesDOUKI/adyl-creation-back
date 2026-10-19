package ld.application.problems.order.accept;

import ld.spring.web.lib.ProblemSpecs;
import ld.domain.features.order.accept.AcceptOrderError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;

public class AcceptOrderProblemMapper implements ProblemMapper<AcceptOrderError> {

    @Override
    public ProblemDetailResponse toProblem(AcceptOrderError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private AcceptOrderProblem toApi(AcceptOrderError error) {
        return switch (error) {
            case AcceptOrderError.OrderAlreadyRejected ignored  -> new AcceptOrderProblem.Rejected();
            case AcceptOrderError.OrderAlreadyDelivered ignored -> new AcceptOrderProblem.Delivered();
            case AcceptOrderError.OrderNotFound e               -> new AcceptOrderProblem.NotFound(e.orderId());
        };
    }
}
