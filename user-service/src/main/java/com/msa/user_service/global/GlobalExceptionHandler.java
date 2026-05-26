package com.msa.user_service.global;

import com.msa.core_common.error.ErrorResponse;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.GlobalResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException (UserErrorCode 기반)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse> handleCustomException(CustomException e) {
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode().getCode(), e.getMessage());
        GlobalResponse response = GlobalResponse.failure(
                e.getErrorCode().getStatus().value(),
                e.getErrorCode().getCode(),
                errorResponse
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    // @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse errorResponse = ErrorResponse.of("VALIDATION_ERROR", errors.toString());
        GlobalResponse response = GlobalResponse.failure(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                errorResponse
        );
        return ResponseEntity.badRequest().body(response);
    }

    // EntityNotFoundException (존재하지 않는 리소스)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GlobalResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", e.getMessage());
        GlobalResponse response = GlobalResponse.failure(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                errorResponse
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 그 외 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse> handleException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        GlobalResponse response = GlobalResponse.failure(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                errorResponse
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
