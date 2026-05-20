package com.msa.hub_service.controller;

import com.msa.hub_service.dto.HubRequest;
import com.msa.hub_service.service.HubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/hubs")
@RequiredArgsConstructor
public class HubController {
    private final HubService hubService;

    @PostMapping
    public UUID createHub(@Valid @RequestBody HubRequest request){
        return hubService.createHub(request.name(),request.address());
    }
}
