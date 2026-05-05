package com.msa.user_service.refresh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
