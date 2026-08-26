package ld.application.read;

import ld.application.response.GetProductResponse;
import ld.standard.lib.validation.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetProductService {
    Page<GetProductResponse> findAll(Pageable pageable);
    Result<GetProductResponse> findById(UUID productId);
}
