package com.msa.stock_service.repository;

import com.msa.stock_service.entity.Stock;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Stock findByProductId(UUID productId);
}
