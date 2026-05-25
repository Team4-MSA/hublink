package com.msa.company_service.repository;

import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID>, CompanyRepositoryCustom {
    boolean existsByHubIdAndNameAndTypeAndAddress(@NotNull(message = "업체 소속 허브는 필수입니다.") UUID uuid, @NotNull(message = "업체 이름은 필수입니다.") String name, @NotNull(message = "업체 유형은 필수입니다.") CompanyType type, @NotNull(message = "업체 주소는 필수입니다.") String address);
}
