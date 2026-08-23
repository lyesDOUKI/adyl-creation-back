package ld.domain.features.product.photos.validation;

import ld.standard.lib.validation.ErrorCode;

public enum PhotoErrorCode implements ErrorCode {
    PHOTO_LIST_EMPTY,
    PHOTO_SIZE_ZERO,
    PHOTO_MAX_SIZE_EXCEEDED,
    PHOTO_EXTENSION_NOT_ALLOWED,
    STORAGE_FAILED
}
