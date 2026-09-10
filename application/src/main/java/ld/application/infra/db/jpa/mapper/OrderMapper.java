package ld.application.infra.db.jpa.mapper;

import ld.application.infra.db.entity.CustomerEntity;
import ld.application.infra.db.entity.DeliveryAddressEntity;
import ld.application.infra.db.entity.OrderDetailEntity;
import ld.application.infra.db.entity.OrderEntity;
import ld.domain.features.order.model.CustomerInfo;
import ld.domain.features.order.model.OrderSnapshot;
import ld.domain.features.product.model.ProductColor;
import ld.domain.valueObjects.Percentage;
import ld.domain.valueObjects.Price;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class OrderMapper {

    private OrderMapper() {}


    public static OrderEntity from(OrderSnapshot orderSnapshot) {
        var customerEntity = toCustomerEntity(orderSnapshot.customerInfo());
        var deliveryAddressEntity = toDeliveryAddressEntity(orderSnapshot.customerInfo());

        return toEntity(orderSnapshot, customerEntity, deliveryAddressEntity);
    }

    public static OrderEntity toEntity(
            OrderSnapshot snapshot,
            CustomerEntity customerEntity,
            DeliveryAddressEntity deliveryAddressEntity
    ) {
        OrderEntity order = new OrderEntity(
                snapshot.orderId(),
                customerEntity,
                deliveryAddressEntity,
                snapshot.message(),
                snapshot.total() != null ? snapshot.total() : BigDecimal.ZERO,
                snapshot.orderStatus()
        );

        if (snapshot.items() != null) {
            snapshot.items().stream()
                    .map(OrderMapper::toDetailEntity)
                    .forEach(order::addDetail);
        }

        return order;
    }

    public static CustomerEntity toCustomerEntity(CustomerInfo info) {
        return new CustomerEntity(
                info.identitySubject(),
                info.email(),
                info.phoneNumber()
        );
    }

    public static DeliveryAddressEntity toDeliveryAddressEntity(CustomerInfo info) {
        return new DeliveryAddressEntity(
                info.address(),
                info.city()
        );
    }

    public static void updateEntity(OrderEntity existingEntity, OrderSnapshot snapshot) {
        existingEntity.setCustomerMessage(snapshot.message());
        existingEntity.setTotal(snapshot.total() != null ? snapshot.total() : BigDecimal.ZERO);
        existingEntity.setStatusData(snapshot.orderStatus());

        if (snapshot.customerInfo() != null) {
            var info = snapshot.customerInfo();

            if (existingEntity.getCustomerEntity() != null) {
                var customer = existingEntity.getCustomerEntity();

                customer.setEmail(info.email());
                customer.setPhone(info.phoneNumber());
            }

            if (existingEntity.getDeliveryAddressEntity() != null) {
                var deliveryAddress = existingEntity.getDeliveryAddressEntity();

                deliveryAddress.setAddress(info.address());
                deliveryAddress.setCity(info.city());
            }
        }

        if (snapshot.items() == null || snapshot.items().isEmpty()) {
            existingEntity.getDetails().clear();
        } else {
            Set<UUID> snapshotIds = snapshot.items().stream()
                    .map(OrderSnapshot.OrderItemSnapshot::itemId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            existingEntity.getDetails().removeIf(detail -> !snapshotIds.contains(detail.getId()));

            for (var item : snapshot.items()) {
                Optional<OrderDetailEntity> existingDetail = existingEntity.getDetails().stream()
                        .filter(detail -> Objects.equals(detail.getId(), item.itemId()))
                        .findFirst();

                existingDetail.ifPresentOrElse(
                        entity -> entity.updateFromSnapshot(item),
                        () -> existingEntity.addDetail(OrderMapper.toDetailEntity(item)));
            }
        }
    }

    public static OrderSnapshot toSnapshot(OrderEntity entity) {
        var customer = entity.getCustomerEntity();
        var deliveryAddress = entity.getDeliveryAddressEntity();

        var customerInfo = new CustomerInfo(
                customer.getIdentitySubject(),
                customer.getEmail(),
                customer.getPhone(),
                deliveryAddress.getAddress(),
                deliveryAddress.getCity()
        );

        var items = entity.getDetails().stream()
                .map(OrderMapper::toItemSnapshot)
                .toList();

        return new OrderSnapshot(
                entity.getId(),
                customerInfo,
                entity.getCustomerMessage(),
                entity.getTotal(),
                entity.getStatusData(),
                items
        );
    }


    private static OrderDetailEntity toDetailEntity(OrderSnapshot.OrderItemSnapshot item) {
        return new OrderDetailEntity(
                item.itemId(),
                item.productId(),
                BigDecimal.valueOf(item.quantity()),
                item.price().value(),
                item.total().value(),
                item.discountRate().value(),
                item.color().value()
        );
    }

    private static OrderSnapshot.OrderItemSnapshot toItemSnapshot(OrderDetailEntity detail) {
        return new OrderSnapshot.OrderItemSnapshot(
                detail.getId(),
                detail.getProductId(),
                new Price(detail.getUnitPrice()),
                detail.getQuantity().intValue(),
                new Price(detail.getTotalAmount()),
                new Percentage(detail.getDiscountRate()),
                new ProductColor(detail.getChosenColor())
        );
    }
}