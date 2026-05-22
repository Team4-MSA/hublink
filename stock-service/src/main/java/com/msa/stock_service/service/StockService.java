package com.msa.stock_service.service;

import com.msa.stock_service.dto.StockDecreaRequestDto;
import com.msa.stock_service.dto.StockRequestDto;
import com.msa.stock_service.entity.Stock;
import com.msa.stock_service.entity.StockChangeReason;
import com.msa.stock_service.entity.StockHistory;
import com.msa.stock_service.repository.StockHistoryRepository;
import com.msa.stock_service.repository.StockRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;

    /**
     * 재고 복원
     * @param listDto
     */
    @Transactional
    public void restoreStock(List<StockDecreaRequestDto> listDto){
        //전달 받은 데이터 리스트를 map으로 변환한다.
        // 한번 요청에 동일한 productId가 올 수 있으므로, 아래와 같이 수량을 합산.
        Map<UUID,Integer> restoreQuantityMap = listDto.stream().collect(Collectors.toMap(
            StockDecreaRequestDto :: getProductId,
            StockDecreaRequestDto :: getQuantity,
            Integer::sum
        ));

        //Map에서 productID 꺼내기
        List<UUID> productIdList = new ArrayList<>(restoreQuantityMap.keySet());

        //복원된 재고 이력 DB에 저장하기 위해 새로운 재고 이력 리스트 생성.
        List<StockHistory> restoreStockHistory = new ArrayList<>();

        //비관적으로 복원할 수량을 가져온다.
        List<Stock> stockList = stockRepository.findAllByProductIdInForUpdate(productIdList);

        for (Stock stock : stockList) {
            //전달받은 데이터에서 수량 가져오기
            Integer restoreQuantity = restoreQuantityMap.getOrDefault(stock.getProductId(), 0);

            // 그 수량이 0인 경우에만
            if (restoreQuantity > 0) {
                // 재고 복원
                stock.restore(restoreQuantity);

                // 복원된 재고의 이력을 만든다.
                StockHistory newHistory = StockHistory.restore(stock, restoreQuantity);
                // 복원된 재고의 이력만 리스트를 만들어
                restoreStockHistory.add(newHistory);
            }
        }
        // DB에 저장한다.
        stockHistoryRepository.saveAll(restoreStockHistory);

    }


    /**
     * 재고 감소 기능
     * 재고 조회 -> 주문 수량과 재고 수량 비교 -> 재고 감소 -> 이에 따른 재고 이력 생성
     * @param listDto
     * @return
     */
    @Transactional
    public List<StockHistory> decreaStock (List<StockDecreaRequestDto> listDto){
        // 전달 받은 데이터를 Map으로 변환, 동일한 productId일 경우 수량을 합산한다.
        Map<UUID, Integer> requestQuantityMap = listDto.stream()
            .collect(Collectors.toMap(
                StockDecreaRequestDto::getProductId,
                StockDecreaRequestDto::getQuantity,
                Integer::sum
            ));
        //변환된 Map에서 ProductId를 추출한다.
        List<UUID> productIdList = new ArrayList<>(requestQuantityMap.keySet());
        //비관락을 사용하여 재고 리스트를 가져온다.
        List<Stock> stockList = stockRepository.findAllByProductIdInForUpdate(productIdList);

        //해당 재고리스트의 크기와 전달받은 데이터의 사이즈가 동일하지 않다면,
        if (stockList.size() != requestQuantityMap.size()) {
            //DB에 없는 상품을 주문한 것으로 간주하여 에러 발생.
            throw new IllegalArgumentException("요청한 상품 중 일부 재고 정보를 찾을 수 없습니다.");
        }

        //DB에 등록할 재고 이력 리스트를 새롭게 생성
        List<StockHistory> newStockHistory = new ArrayList<>();

        //재고리스트를 순회하며
        for (Stock stock : stockList) {
            //전달받은 데이터 중 주문한 수량을 꺼내서
            Integer requestQuantity = requestQuantityMap.get(stock.getProductId());

            //현재 재고의 수량이 주문한 수량보다 더 크다면
            if (stock.getQuantity() >= requestQuantity) {
                // 정상 차감 및 이력 생성.
                stock.decreaStock(requestQuantity);
                newStockHistory.add(StockHistory.createDecreaStock(stock, requestQuantity));
            } else {
                // 그 외로 재고가 부족하다면, 에러를 일으킨다.
                throw new RuntimeException("재고가 부족합니다. 상품 ID: " + stock.getProductId());
            }
        }

        // 생성된 재고 이력을 DB에 모두 저장시킨다.
        stockHistoryRepository.saveAll(newStockHistory);

        return newStockHistory;
    }

    /**
     * 재고 생성 및 재고 이력을 생성
     * @param dto
     * @return
     */
    @Transactional
    public Stock createStock(StockRequestDto dto){
        // 재고를 DB에 생성한다.
        Stock newStock = Stock.create(dto);
        stockRepository.save(newStock);

        // 재고 이력을 DB에 생성한다.
        StockHistory newStockHistory = StockHistory.create(newStock);
        stockHistoryRepository.save(newStockHistory);

        return newStock;
    }

}
