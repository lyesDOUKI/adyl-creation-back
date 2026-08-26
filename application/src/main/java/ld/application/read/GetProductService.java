package ld.application.read;

import ld.application.response.GetProductsResponse;

import java.util.List;

public interface GetProductService {
    List<GetProductsResponse> findAll();
}
