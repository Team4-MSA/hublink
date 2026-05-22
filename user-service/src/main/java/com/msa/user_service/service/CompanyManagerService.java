package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.dto.CompanyManagerRequest;
import com.msa.user_service.dto.CompanyManagerResponse;
import com.msa.user_service.entity.CompanyManager;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.CompanyManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyManagerService {

    private final CompanyManagerRepository companyManagerRepository;

    // 업체 담당자 등록
    @Transactional
    public CompanyManagerResponse register(CompanyManagerRequest request) {
        CompanyManager companyManager = CompanyManager.builder()
                .userId(request.getUserId())
                .companyId(request.getCompanyId())
                .build();
        return CompanyManagerResponse.from(companyManagerRepository.save(companyManager));
    }

    // 목록 조회
    public PageRes<CompanyManagerResponse> getList(UUID companyId, Pageable pageable) {
        if (companyId != null) {
            return new PageRes<>(companyManagerRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                    .map(CompanyManagerResponse::from));
        }
        return new PageRes<>(companyManagerRepository.findAllByDeletedAtIsNull(pageable)
                .map(CompanyManagerResponse::from));
    }

    // 상세 조회
    public CompanyManagerResponse getOne(UUID companyManagerId) {
        CompanyManager companyManager = companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(companyManagerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.COMPANY_MANAGER_NOT_FOUND));
        return CompanyManagerResponse.from(companyManager);
    }

    // 삭제
    @Transactional
    public void delete(UUID companyManagerId, String deletedBy) {
        CompanyManager companyManager = companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(companyManagerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.COMPANY_MANAGER_NOT_FOUND));
        companyManager.delete(deletedBy);
    }
}
