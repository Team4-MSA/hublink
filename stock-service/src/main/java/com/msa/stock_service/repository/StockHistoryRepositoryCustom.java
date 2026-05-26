package com.msa.stock_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.stock_service.dto.StockHistorySearchResponseDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface StockHistoryRepositoryCustom {
    PageRes<StockHistorySearchResponseDto> searchHistoriesByproductId(UUID productId, Pageable pageable);
}
