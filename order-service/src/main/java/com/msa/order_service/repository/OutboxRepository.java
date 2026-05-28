package com.msa.order_service.repository;

import com.msa.order_service.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {


    List<Outbox> findByProcessedFalse();

}
