package com.msa.user_service.repository;

import com.msa.user_service.entity.CompanyManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyManagerRepository extends JpaRepository<CompanyManager, UUID> {

    Optional<CompanyManager> findByCompanyManagerIdAndDeletedAtIsNull(UUID companyManagerId);

    Optional<CompanyManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    Page<CompanyManager> findAllByDeletedAtIsNull(Pageable pageable);

    Page<CompanyManager> findAllByCompanyIdAndDeletedAtIsNull(UUID companyId, Pageable pageable);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndCompanyIdAndDeletedAtIsNull(UUID userId, UUID companyId);
}
