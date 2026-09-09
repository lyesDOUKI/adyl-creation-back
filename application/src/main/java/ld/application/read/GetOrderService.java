package ld.application.read;

import ld.application.response.GetOrderResponse;
import ld.standard.lib.validation.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetOrderService {
    Page<GetOrderResponse> findAll(Pageable pageable);
    Result<GetOrderResponse> findById(UUID productId);
}
