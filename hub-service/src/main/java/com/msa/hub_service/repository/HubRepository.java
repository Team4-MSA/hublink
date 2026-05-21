package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface HubRepository extends JpaRepository<HubEntity, UUID> {
    List<HubEntity> findByLatitudeIsNull();

    Page<HubEntity> findByNameContaining(String name, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByAddress(String address);
}
