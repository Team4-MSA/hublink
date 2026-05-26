package com.msa.company_service.repository;

import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID>, CompanyRepositoryCustom {
    boolean existsByHubIdAndNameAndTypeAndAddress(UUID hubId, String name, CompanyType type, String address);
}
