package com.msa.company_service.service;

import com.msa.company_service.client.HubClient;
import com.msa.company_service.dto.*;
import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyInfo;
import com.msa.company_service.entity.CompanyType;
import com.msa.company_service.global.CompanyErrorCode;
import com.msa.company_service.repository.CompanyRepository;
import com.msa.core_common.auth.UserRole;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final HubClient hubClient;
    private final AuditorAware<String> auditorAware;

    // 생성
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request, UserRole role, UUID userHubId) {

        // 권한 검증
        if (role == UserRole.HUB_MANAGER) {
            if (userHubId == null || !userHubId.equals(request.hubId())) {
                throw new CustomException(CompanyErrorCode.FORBIDDEN);
            }
        }


        if (companyRepository.existsByHubIdAndNameAndTypeAndAddress(
                request.hubId(),
                request.name(),
                request.type(),
                request.address())) {
            throw new CustomException(CompanyErrorCode.COMPANY_NAME_DUPLICATED);
        }

        validateHubId(request.hubId());

        CoordinateDto coordinate;
        if (request.latitude() != null && request.longitude() != null) {
            coordinate = new CoordinateDto(request.latitude(), request.longitude());
        } else {
            coordinate = hubClient.getCoordinates(request.address());
        }

        CompanyInfo info = new CompanyInfo(
                request.hubId(),
                request.name(),
                request.type(),
                request.address(),
                coordinate.latitude(),
                coordinate.longitude()
        );

        CompanyEntity company = CompanyEntity.create(info);

        companyRepository.save(company);
        return CompanyResponse.from(company);
    }

    // 단건 조회
    public CompanyResponse getCompany(UUID companyId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(CompanyErrorCode.COMPANY_NOT_FOUND));
        return CompanyResponse.from(company);
    }

    // 삭제
    @Transactional
    public CompanyResponse deleteCompany(UUID companyId, UserRole role, UUID userHubId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(CompanyErrorCode.COMPANY_NOT_FOUND));

        verifyOwnership(company, null, role, userHubId);

        String deletedBy = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        company.delete(deletedBy);

        return CompanyResponse.from(company);
    }

    // 수정
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest request, UUID userCompanyId, UserRole role, UUID userHubId) {

        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(CompanyErrorCode.COMPANY_NOT_FOUND));

        verifyOwnership(company, userCompanyId, role, userHubId);

        if (request.hubId() != null && !request.hubId().equals(company.getHubId())) {
            validateHubId(request.hubId());
        }

        boolean isAddressChanged = request.address() != null && !request.address().equals(company.getAddress());

        BigDecimal targetLat;
        BigDecimal targetLon;

        if (isAddressChanged) { // 주소가 바뀐 경우
            if (request.latitude() != null && request.longitude() != null) {
                targetLat = request.latitude();
                targetLon = request.longitude();
            } else {
                CoordinateDto apiCoordinate = hubClient.getCoordinates(request.address());
                targetLat = apiCoordinate.latitude();   // API 실패 시 null
                targetLon = apiCoordinate.longitude();  // API 실패 시 null
            }
        } else { // 바뀌지 않은 경우
            targetLat = request.latitude() != null ? request.latitude() : company.getLatitude();
            targetLon = request.longitude() != null ? request.longitude() : company.getLongitude();
        }

        CoordinateDto coordinate = new CoordinateDto(targetLat, targetLon);

        CompanyInfo info = new CompanyInfo(
                request.hubId(),
                request.name(),
                request.type(),
                request.address(),
                coordinate.latitude(),
                coordinate.longitude()
        );

        company.update(info);

        return CompanyResponse.from(company);
    }

    // 검색
    public PageRes<CompanyResponse> getCompanies(UUID hubId, String name, CompanyType type, String address, Pageable pageable) {

        Page<CompanyEntity> companyPage = companyRepository.searchCompanies(hubId, name, type, address, pageable);

        return new PageRes<>(companyPage.map(CompanyResponse::from));
    }

    // 업체를 허브 루트에 포함시키기 위해 전달
    public CompanyDto getCompanyLoc(UUID companyId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(CompanyErrorCode.COMPANY_NOT_FOUND));

        return new CompanyDto(
                company.getAddress(),
                company.getLatitude(),
                company.getLongitude(),
                company.getHubId()
        );
    }

    // 업체 존재 여부
    public boolean getCompanyExists(UUID companyId) {
        return companyRepository.existsById(companyId);
    }

    // 업체 이름 확인
    public List<CompanyNameResponse> getCompanyNames(List<UUID> companyIds) {

        if (companyIds == null || companyIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompanyEntity> companies = companyRepository.findAllById(companyIds);

        return companies.stream()
                .map(company -> new CompanyNameResponse(
                        company.getCompanyId(),
                        company.getName()
                ))
                .toList();
    }

    // 허브 아이디 확인
    private void validateHubId(UUID hubId) {
        if (!hubClient.getHubExist(hubId)) {
            throw new CustomException(CompanyErrorCode.HUB_NOT_FOUND);
        }
    }

    // 소유권 검증
    private void verifyOwnership(CompanyEntity company, UUID userCompanyId, UserRole role, UUID userHubId) {

        if (role == UserRole.MASTER) {
            return;
        }

        // 담당 허브 업체 확인
        if (role == UserRole.HUB_MANAGER) {
            if (!company.getHubId().equals(userHubId)) {
                throw new CustomException(CompanyErrorCode.FORBIDDEN);
            }
            return;
        }

        // 담당 업체 확인
        if (role == UserRole.COMPANY_MANAGER) {
            if (!company.getCompanyId().equals(userCompanyId)) {
                throw new CustomException(CompanyErrorCode.FORBIDDEN);
            }
            return;
        }

        // 그 외의 경우 차단
        throw new CustomException(CompanyErrorCode.FORBIDDEN);
    }
}
