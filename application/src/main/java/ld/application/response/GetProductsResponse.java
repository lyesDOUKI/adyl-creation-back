package ld.application.response;

import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.util.List;

public record GetProductsResponse(String name, BigDecimal price, List<String> colors,
                                  List<String> photosUri, int numberOfOrders) implements ApiResponseBody {
}
