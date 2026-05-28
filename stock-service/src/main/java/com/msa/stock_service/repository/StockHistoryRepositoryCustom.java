package com.msa.stock_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.stock_service.dto.StockHistorySearchResponseDto;
import com.msa.stock_service.entity.StockHistory;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockHistoryRepositoryCustom {
    Page<StockHistory> searchHistoriesByproductId(UUID productId, Pageable pageable);
}
