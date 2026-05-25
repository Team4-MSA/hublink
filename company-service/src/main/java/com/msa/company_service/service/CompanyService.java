package com.msa.company_service.service;

import com.msa.company_service.client.HubClient;
import com.msa.company_service.dto.*;
import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;
import com.msa.company_service.global.CompanyErrorCode;
import com.msa.company_service.repository.CompanyRepository;
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
    public CompanyResponse createCompany(CompanyRequest request) {

        validateHubId(request.hubId());

        CoordinateDto coordinateDto;
        if (request.latitude() != null && request.longitude() != null) {
            coordinateDto = new CoordinateDto(request.latitude(), request.longitude());
        } else {
            coordinateDto = hubClient.getCoordinates(request.address()).getData();
        }

        CompanyEntity company = CompanyEntity.create(request, coordinateDto);

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
    public CompanyResponse deleteCompany(UUID companyId) {
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(CompanyErrorCode.COMPANY_NOT_FOUND));
        String deletedBy = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        company.delete(deletedBy);

        return CompanyResponse.from(company);
    }

    // 수정
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyUpdateRequest request) {

        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(CompanyErrorCode.COMPANY_NOT_FOUND));

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
                CoordinateDto apiCoordinate = hubClient.getCoordinates(request.address()).getData();
                targetLat = apiCoordinate.latitude();   // API 실패 시 null
                targetLon = apiCoordinate.longitude();  // API 실패 시 null
            }
        } else { // 바뀌지 않은 경우
            targetLat = request.latitude() != null ? request.latitude() : company.getLatitude();
            targetLon = request.longitude() != null ? request.longitude() : company.getLongitude();
        }

        CoordinateDto coordinate = new CoordinateDto(targetLat, targetLon);

        company.update(request, coordinate);

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
                company.getLongitude()
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
        if (!hubClient.getHubExist(hubId).getData()) {
            throw new CustomException(CompanyErrorCode.HUB_NOT_FOUND);
        }
    }
}
