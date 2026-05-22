package com.msa.order_service.feign;

import com.msa.order_service.dto.res.UsernameResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserFeignClient {

    @GetMapping("/namesAndCompanies")
    public List<UsernameResDto> getUserNames(@RequestParam List<UUID> userIds);

}
