package ld.application.infra.db.jpa.adapter;

import ld.application.infra.db.entity.CustomerEntity;
import ld.application.infra.db.entity.DeliveryAddressEntity;
import ld.application.infra.db.entity.OrderEntity;
import ld.application.infra.db.jpa.OrderJpaRepository;
import ld.application.infra.db.jpa.mapper.OrderMapper;
import ld.domain.features.order.lifecycle.OrderEditor;
import ld.domain.features.order.model.OrderSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderEditorJpaAdapter implements OrderEditor {

    private final OrderJpaRepository orderJpaRepository;

    public OrderEditorJpaAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public void save(OrderSnapshot orderSnapshot) {
        orderJpaRepository.findById(orderSnapshot.orderId())
                .ifPresentOrElse(
                        existingEntity -> OrderMapper.updateEntity(existingEntity, orderSnapshot),
                        () -> {
                            CustomerEntity customer = OrderMapper.toCustomerEntity(orderSnapshot.customerInfo());
                            DeliveryAddressEntity deliveryAddress = OrderMapper.toDeliveryAddressEntity(orderSnapshot.customerInfo());
                            OrderEntity newEntity = OrderMapper.toEntity(
                                    orderSnapshot,
                                    customer,
                                    deliveryAddress
                            );
                            orderJpaRepository.save(newEntity);
                        }
                );
    }

    @Override
    public Optional<OrderSnapshot> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .map(OrderMapper::toSnapshot);
    }
}