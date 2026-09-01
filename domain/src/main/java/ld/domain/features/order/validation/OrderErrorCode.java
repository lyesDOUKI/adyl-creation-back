package ld.domain.features.order.validation;

import ld.standard.lib.validation.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    PRODUCT_COLOR_NOT_AVAILABLE,
    PRODUCTS_NOT_FOUND,
    PRODUCT_NOT_AVAILABLE,
    ORDER_NOT_FOUND,
    ORDER_HAS_BEEN_DELIVERED,
    PENDING_ORDER, ORDER_HAS_BEEN_REJECTED
}
