package com.msa.hub_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.hub_service.dto.HubRequest;
import com.msa.hub_service.dto.HubResponse;
import com.msa.hub_service.service.HubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/hubs")
@RequiredArgsConstructor
public class HubController {
    private final HubService hubService;

    // 허브 생성
    @PostMapping
    public HubResponse createHub(@Valid @RequestBody HubRequest request){
        return hubService.createHub(request.name(),request.address());
    }

    //허브 상세 조회
    @GetMapping("{hubId}")
    public HubResponse getHub(@PathVariable UUID hubId){
        return hubService.getHub(hubId);
    }

    // 허브 수정
    @PatchMapping("/{hubId}")
    public HubResponse updateHub(@PathVariable UUID hubId, @Valid @RequestBody HubRequest request){
        return hubService.updateHub(hubId, request.name(), request.address());
    }

    // 허브 검색
    @GetMapping
    public PageRes<HubResponse> getHubs(
            @RequestParam(required = false) String name,
            @PageableDefault(page = 0, size = 10, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return hubService.getHubs(name, pageable);
    }

    // 허브 삭제
    @DeleteMapping("/{hubId}")
    public HubResponse deleteHub(
            @PathVariable UUID hubId
    ){
        return hubService.deleteHub(hubId);
    }
}
