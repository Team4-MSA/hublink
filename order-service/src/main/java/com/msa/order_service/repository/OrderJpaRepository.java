package com.msa.order_service.repository;

import com.msa.order_service.entity.Orders;
import com.msa.order_service.type.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<Orders, UUID> {

    @Query("SELECT o FROM Orders o WHERE (:status IS NULL OR o.status = :status) AND o.supplierCompanyId = :companyId")
    Page<Orders> findAllByStatusAndSupplierCompanyId(
            @Param("status") Status status,
            @Param("companyId") UUID companyId,
            Pageable pageable
    );

    @Query("SELECT o FROM Orders o WHERE (:status IS NULL OR o.status = :status) AND o.receiverCompanyId = :companyId")
    Page<Orders> findAllByStatusAndReceiverCompanyId(
            @Param("status") Status status,
            @Param("companyId") UUID companyId,
            Pageable pageable
    );

    @Query("select o from Orders o join fetch o.orderItems where o.id = :orderId")
    Optional<Orders> findByOrderId(UUID orderId);

}

