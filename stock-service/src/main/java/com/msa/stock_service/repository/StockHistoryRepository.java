package com.msa.stock_service.repository;

import com.msa.stock_service.entity.StockHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID> {
    StockHistory findByProductId(UUID productId);
}
