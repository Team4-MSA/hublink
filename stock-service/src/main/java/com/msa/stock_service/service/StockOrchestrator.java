package com.msa.stock_service.service;

import com.msa.stock_service.client.ProductClient;
import com.msa.stock_service.client.ProductResponse;
import com.msa.stock_service.dto.StockDecreaRequestDto;
import com.msa.stock_service.dto.StockHistoryResponseDto;
import com.msa.stock_service.entity.StockChangeReason;
import com.msa.stock_service.entity.StockHistory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockOrchestrator {
    private final ProductClient productClient;
    private final StockService stockService;

    /**
     * 재고 감소 -> 재고 이력 생성
     * 이 재고에 해당하는 가격과 이름을 달아 반환한다.
     * @param listDto
     * @return
     */
    public List<StockHistoryResponseDto> decreaseStock (List<StockDecreaRequestDto> listDto){
        // 재고 감소를 하고, 그에 대한 재고 이력을 만든 다음, 재고 이력 리스트를 가져온다.
         List<StockHistory> histories = stockService.decreaseStock(listDto);

        //외부 서비스로 productId 리스트에 해당하는 상품 리스트를 가져온다.
         List<UUID> productIdList = histories.stream().map(StockHistory::getProductId).collect(Collectors.toList());
        List<ProductResponse> productList = productClient.getProductsById(productIdList);

        //이 상품 목록을 Map으로 변환한다.
        Map<UUID, ProductResponse> productMap = productList.stream().collect(Collectors.toMap(ProductResponse::getProductId, product -> product));

        //반환할 StockHistoryResponseDto 리스트를 만든다.
        List<StockHistoryResponseDto> stockHistoryResponseDtos = new ArrayList<>();

        //StockHistory 리스트를 반복해서,
        for(StockHistory  history: histories) {
            //먼저 특정 history에 해당하는 ProductResponse(상품 정보)를 가져온다.
            ProductResponse productResponse = productMap.get(history.getProductId());
            //StockHistoryResponseDto에 넣을 재고 감소 성공 여부를 만든다.
            boolean isSuccess = true;

            //이제 반환할 StockHistoryResponseDto(상품 정보가 포함된 이력)를 하나씩 만든다.
            StockHistoryResponseDto result = StockHistoryResponseDto.from(history,isSuccess,productResponse.getName(),productResponse.getPrice());
            //만든 StockHistoryResponseDto을 StockHistoryResponseDto 리스트에 저장한다.
            stockHistoryResponseDtos.add(result);
        }
        return stockHistoryResponseDtos;
    }
}
