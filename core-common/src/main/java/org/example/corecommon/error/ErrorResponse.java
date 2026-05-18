package org.example.corecommon.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String errorClassName;
    private final String message;

    public static ErrorResponse of(String errorClassName, String message) {
        return new ErrorResponse(errorClassName, message);
    }
}
