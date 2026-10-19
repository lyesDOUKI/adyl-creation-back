package ld.application.api.handler;

import ld.standard.lib.validation.ErrorCode;

public enum RequestErrorCode implements ErrorCode {
    VALIDATION_FAILED,
    INVALID_SORT_FIELD,
    INVALID_MULTIPART_REQUEST
}