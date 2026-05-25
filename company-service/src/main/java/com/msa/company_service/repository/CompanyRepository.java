package com.msa.company_service.repository;

import com.msa.company_service.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID>, CompanyRepositoryCustom {
}
