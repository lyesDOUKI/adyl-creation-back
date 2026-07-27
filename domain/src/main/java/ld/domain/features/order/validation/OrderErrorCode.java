package ld.domain.features.order.validation;

import ld.lib.validation.ErrorCode;

public enum OrderErrorCode implements ErrorCode {

    PRODUCT_COLOR_NOT_AVAILABLE,
    PRODUCTS_NOT_FOUND,
    PRODUCT_NOT_AVAILABLE;
}
