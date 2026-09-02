package ld.application.response;

import ld.spring.web.lib.ApiResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetProductResponse(UUID productId,
                                 ProductCategoryResponse productCategory,
                                 String name,
                                 BigDecimal price, List<String> colors,
                                 List<String> photosUri, int numberOfOrders) implements ApiResponseBody {
}
