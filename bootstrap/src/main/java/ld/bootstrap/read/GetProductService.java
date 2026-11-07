package ld.bootstrap.read;

import ld.bootstrap.response.GetProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetProductService {
    Page<GetProductResponse> findAll(Pageable pageable);
    Optional<GetProductResponse> findById(UUID productId);
}
