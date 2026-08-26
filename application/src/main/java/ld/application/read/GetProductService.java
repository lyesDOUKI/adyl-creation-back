package ld.application.read;

import ld.application.response.GetProductResponse;

import java.util.List;

public interface GetProductService {
    List<GetProductResponse> findAll();
}
