package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.Order;
import ld.application.infra.db.entity.OrderDetail;
import ld.domain.features.order.model.OrderSnapshot;

import java.math.BigDecimal;

public class OrderMapper {
    private OrderMapper(){}

    public static Order from(OrderSnapshot orderSnapshot) {
        var customerInfo = orderSnapshot.customer();
        var order = new Order(
                orderSnapshot.orderId(),
                customerInfo.name(),
                customerInfo.email(),
                customerInfo.phoneNumber()
        );
        order.setCustomerAddress(customerInfo.address());
        order.setCustomerCity(customerInfo.city());
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
