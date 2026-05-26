package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.dto.CompanyManagerRequest;
import com.msa.user_service.dto.CompanyManagerResponse;
import com.msa.user_service.service.CompanyManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.global.UserErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company-managers")
@RequiredArgsConstructor
public class CompanyManagerController {

    private final CompanyManagerService companyManagerService;

    @PostMapping
    public ResponseEntity<CompanyManagerResponse> register(
            @Valid @RequestBody CompanyManagerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyManagerService.register(request));
    }

    @GetMapping
    public ResponseEntity<PageRes<CompanyManagerResponse>> getList(
            @RequestParam(required = false) UUID companyId,
            Pageable pageable,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(companyManagerService.getList(companyId, pageable));
    }

    @GetMapping("/{companyManagerId}")
    public ResponseEntity<CompanyManagerResponse> getOne(
            @PathVariable UUID companyManagerId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !role.equals("COMPANY_MANAGER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(companyManagerService.getOne(companyManagerId));
    }

    @DeleteMapping("/{companyManagerId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID companyManagerId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        companyManagerService.delete(companyManagerId, userId);
        return ResponseEntity.noContent().build();
    }
}
