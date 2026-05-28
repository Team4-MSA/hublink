package com.msa.stock_service.service;

import com.msa.stock_service.client.ProductClient;
import com.msa.stock_service.client.ProductResponse;
import com.msa.stock_service.dto.StockDecreaRequestDto;
import com.msa.stock_service.dto.StockHistoryResponseDto;
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
        Map<UUID, StockDecreaRequestDto> mapDto = new HashMap<>();
        for(StockDecreaRequestDto dto: listDto) {
            mapDto.put(dto.getId(), dto);
        }

        // 재고 감소 -> 재고 이력 작성 -> 재고 이력 리스트 반환.
         List<StockHistory> histories = stockService.decreaseStock(listDto);

        //재고 이력 리스트 안의 상품 ID를 추출하여 리스트로 변환 후,
         List<UUID> productIdList = histories.stream().map(StockHistory::getProductId).collect(Collectors.toList());
         //이 상품 ID 리스트를 이용하여, 상품 목록을 가져온다.
        List<ProductResponse> productList = productClient.getProductsById(productIdList);

        //이 상품 목록을 Map으로 변환한다.
        Map<UUID, ProductResponse> productMap = productList.stream().collect(Collectors.toMap(ProductResponse::getProductId, product -> product));

        //반환할 StockHistoryResponseDto 리스트를 만든다.
        List<StockHistoryResponseDto> stockHistoryResponseDtos = new ArrayList<>();

        //재고 이력목록을 반복해서,
        for(StockHistory  history: histories) {
            //그 재고 이력에 해당하는 상품 정보를 가져온다.
            ProductResponse productResponse = productMap.get(history.getProductId());
            //이 상품 정보들은 전부 성공 여부가 true인 것들이고,
            boolean isSuccess = true;

            //이제 반환할 StockHistoryResponseDto(상품 정보가 포함된 이력)를 하나씩 만든다.
            StockHistoryResponseDto result = StockHistoryResponseDto.from(history,isSuccess,productResponse.getName(),productResponse.getPrice(),mapDto.get(history.getProductId()));
            //만든 StockHistoryResponseDto을 StockHistoryResponseDto 리스트에 저장한다.
            stockHistoryResponseDtos.add(result);
        }
        return stockHistoryResponseDtos;
    }
}
