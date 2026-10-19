package ld.application.problems.order.reject;

import ld.domain.features.order.reject.RejectOrderError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.ProblemSpecs;

public class RejectOrderProblemMapper implements ProblemMapper<RejectOrderError> {

    @Override
    public ProblemDetailResponse toProblem(RejectOrderError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private RejectOrderProblem toApi(RejectOrderError error) {
        return switch (error) {
            case RejectOrderError.OrderAlreadyDelivered ignored ->
                    new RejectOrderProblem.orderAlreadyDelivered();

            case RejectOrderError.OrderNotFound e ->
                    new RejectOrderProblem.orderNotFound(e.orderId());
        };
    }
}