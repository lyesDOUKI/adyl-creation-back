package ld.application.read;

import ld.application.response.GetOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetOrderService {
    Page<GetOrderResponse> findAll(Pageable pageable, UUID customerId);
    Optional<GetOrderResponse> findById(UUID orderId, UUID customerId);
}