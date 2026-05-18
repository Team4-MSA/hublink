package com.msa.user_service.refresh;

import com.msa.user_service.dto.ExamDto;
import org.example.corecommon.error.exception.CustomException;
import org.example.corecommon.error.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RefreshScope
@RestController
@RequestMapping("/users")
public class RefreshController {

    @Value("${message}")
    private String message;

    @GetMapping("/msg")
    public String msg() {
        return message;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("msg", message);
    }

    @GetMapping("/errorTest")
    public void errorTest() {
        throw new CustomException(ErrorCode.TEST_ERROR_CODE);
    }

    @PostMapping("/validTest")
    public void validTest (@RequestBody @Validated ExamDto examDto){

    }
}
