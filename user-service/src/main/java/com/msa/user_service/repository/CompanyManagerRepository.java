package com.msa.user_service.repository;

import com.msa.user_service.entity.CompanyManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyManagerRepository extends JpaRepository<CompanyManager, UUID> {

    Optional<CompanyManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);
}
