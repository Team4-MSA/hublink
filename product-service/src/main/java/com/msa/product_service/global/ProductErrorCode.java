package com.msa.product_service.global;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {;
    private final String code;
    private final String message;
    private final HttpStatus status;

}
