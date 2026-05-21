package com.msa.delivery_service.infrastructure.client;

import com.msa.delivery_service.domain.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
