package com.msa.stock_service.repository;

import com.msa.stock_service.entity.StockHistory;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID>,StockHistoryRespositoryCustom {
    StockHistory findByProductId(UUID productId);

    List<StockHistory> findAllByStockIdIn(List<UUID> stockIds);
}
