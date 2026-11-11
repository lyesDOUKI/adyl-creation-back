package ld.bootstrap.api.hal.order;

import ld.bootstrap.api.order.OrderController;
import ld.bootstrap.read.view.OrderView;
import ld.bootstrap.response.GetOrderResponse;
import org.springframework.hateoas.EntityModel;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class OrderModelAssembler {

    public static EntityModel<GetOrderResponse> toModel(OrderView view) {
        UUID id = view.response().orderId();

        EntityModel<GetOrderResponse> model = EntityModel.of(
                view.response());

        if (view.status().canBeCancelled()) {
            model.add(linkTo(methodOn(OrderController.class).cancelOrder(id))
                    .withRel(OrderLinkRel.CANCEL.rel()));
        }
        return model;
    }
}