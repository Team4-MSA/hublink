package com.msa.stock_service.repository;

import com.msa.stock_service.entity.Stock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


    @Repository
    public interface StockRepository extends JpaRepository<Stock, UUID> {
        @Lock(LockModeType.PESSIMISTIC_WRITE) //FOR UPDATE - 비관적 락 걸기.
        @Query("SELECT s FROM Stock s WHERE s.productId IN :productIds ORDER BY s.id")
        List<Stock> findAllByProductIdInForUpdate(@Param("productIds") List<UUID> productIds);
    Stock findByProductId(UUID productId);
}
