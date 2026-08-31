package ld.application.infra.db.mapper;

import ld.application.infra.db.entity.CustomerEntity;
import ld.application.infra.db.entity.OrderEntity;
import ld.application.infra.db.entity.OrderDetailEntity;
import ld.domain.features.order.model.OrderSnapshot;

import java.math.BigDecimal;

public class OrderMapper {
    private OrderMapper(){}

    public static OrderEntity from(OrderSnapshot orderSnapshot) {
        var customerInfo = orderSnapshot.customerInfo();
        var customer = new CustomerEntity(
                customerInfo.name(),
                customerInfo.email(),
                customerInfo.phoneNumber(),
                customerInfo.address(),
                customerInfo.city()
        );
        var order = new OrderEntity(
                orderSnapshot.orderId(),
                customer
        );
        order.setCustomerMessage(orderSnapshot.message());
        orderSnapshot.items()
                .stream()
                .map(OrderMapper::from)
                .forEach(order::addDetail);
        return order;
    }

    private static OrderDetailEntity from(OrderSnapshot.OrderItemSnapshot itemSnapshot) {
        return new OrderDetailEntity(
                itemSnapshot.itemId(),
                itemSnapshot.productId(),
                BigDecimal.valueOf(itemSnapshot.quantity()),
                itemSnapshot.price().value(),
                itemSnapshot.total().value(),
                itemSnapshot.color().value()
        );
    }
}
