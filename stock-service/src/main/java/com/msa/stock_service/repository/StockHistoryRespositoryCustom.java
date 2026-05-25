package com.msa.stock_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.stock_service.dto.StockHistoryResponseDto;
import com.msa.stock_service.dto.StockHistorySearchResponseDto;
import com.msa.stock_service.dto.StockResponseDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface StockHistoryRespositoryCustom {
    PageRes<StockHistorySearchResponseDto> searchHistoriesByproductId(UUID productId, Pageable pageable);
}
