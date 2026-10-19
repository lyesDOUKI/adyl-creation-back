package ld.application.problems.order;

import ld.standard.lib.validation.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    PRODUCT_COLOR_NOT_AVAILABLE,
    PRODUCTS_NOT_FOUND,
    PRODUCT_NOT_AVAILABLE,
    ORDER_NOT_FOUND,
    ORDER_ALREADY_DELIVERED,
    ORDER_ALREADY_REJECTED,
    ORDER_STILL_PENDING
}
