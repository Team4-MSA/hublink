package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.CompanyClient;
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
    private final CompanyClient companyClient;

    public void validateCompanyExists(UUID companyId) {
        if (!companyClient.checkCompanyExists(companyId).isExists()) {
            throw new CustomException(UserErrorCode.COMPANY_NOT_FOUND);
        }
    }

    @Transactional
    public CompanyManagerResponse register(CompanyManagerRequest request) {
        validateCompanyExists(request.getCompanyId());

        CompanyManager companyManager = saveCompanyManager(request.getUserId(), request.getCompanyId());
        return CompanyManagerResponse.from(companyManager);
    }

    // 승인 흐름 전용
    @Transactional
    public void createOnApproval(UUID userId, UUID companyId) {
        saveCompanyManager(userId, companyId);
    }

    private CompanyManager saveCompanyManager(UUID userId, UUID companyId) {
        return companyManagerRepository.save(CompanyManager.builder()
                .userId(userId)
                .companyId(companyId)
                .build());
    }


    // UserService Internal API용 - 업체 소속 여부 확인
    public boolean existsByUserIdAndCompanyId(UUID userId, UUID companyId) {
        return companyManagerRepository.existsByUserIdAndCompanyIdAndDeletedAtIsNull(userId, companyId);
    }

    public PageRes<CompanyManagerResponse> getList(UUID companyId, Pageable pageable) {
        if (companyId != null) {
            return new PageRes<>(companyManagerRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                    .map(CompanyManagerResponse::from));
        }
        return new PageRes<>(companyManagerRepository.findAllByDeletedAtIsNull(pageable)
                .map(CompanyManagerResponse::from));
    }

    public CompanyManagerResponse getOne(UUID companyManagerId) {
        CompanyManager companyManager = companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(companyManagerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.COMPANY_MANAGER_NOT_FOUND));
        return CompanyManagerResponse.from(companyManager);
    }

    @Transactional
    public void delete(UUID companyManagerId, String deletedBy) {
        CompanyManager companyManager = companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(companyManagerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.COMPANY_MANAGER_NOT_FOUND));
        companyManager.delete(deletedBy);
    }
}
