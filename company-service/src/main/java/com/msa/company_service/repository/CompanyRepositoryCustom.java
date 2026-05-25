package com.msa.company_service.repository;

import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyRepositoryCustom {
    Page<CompanyEntity> searchCompanies(UUID hubId, String name, CompanyType type, String address, Pageable pageable);
}
