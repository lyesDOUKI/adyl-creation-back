package ld.bootstrap.read.view;

import ld.bootstrap.response.GetOrderResponse;
import ld.domain.features.order.model.OrderStatus;

public record OrderView(GetOrderResponse response, OrderStatus status) {}