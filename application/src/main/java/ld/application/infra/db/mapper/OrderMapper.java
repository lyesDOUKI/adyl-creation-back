package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.Order;
import ld.application.infra.db.entity.OrderDetail;
import ld.domain.features.order.model.OrderSnapshot;

import java.math.BigDecimal;

public class OrderMapper {
    private OrderMapper(){}

    public static Order from(OrderSnapshot orderSnapshot) {
        var order = new Order(
                orderSnapshot.orderId(),
                orderSnapshot.name(),
                orderSnapshot.email(),
                orderSnapshot.phone()
        );
        order.setCustomerAddress(orderSnapshot.address());
        order.setCustomerCity(orderSnapshot.city());
        order.setCustomerMessage(orderSnapshot.message());
        orderSnapshot.items()
                .stream()
                .map(OrderMapper::from)
                .forEach(order::addDetail);
        return order;
    }

    private static OrderDetail from(OrderSnapshot.OrderItemSnapshot itemSnapshot) {
        return new OrderDetail(
                itemSnapshot.itemId(),
                itemSnapshot.productId(),
                BigDecimal.valueOf(itemSnapshot.quantity()),
                itemSnapshot.price(),
                itemSnapshot.total(),
                itemSnapshot.color().value()
        );
    }
}
