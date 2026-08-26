package ld.application.read;

import ld.application.response.GetProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetProductService {
    Page<GetProductResponse> findAll(Pageable pageable);
}
