package com.msa.stock_service.service;

import com.msa.stock_service.dto.StockDecreaRequestDto;
import com.msa.stock_service.dto.StockRequestDto;
import com.msa.stock_service.entity.Stock;
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

    /**
     * 재고 감소 기능
     * 재고 조회 -> 주문 수량과 재고 수량 비교 -> 재고 감소 -> 이에 따른 재고 이력 생성
     * @param listDto
     * @return
     */
    @Transactional
    public List<StockHistory> decreaStock (List<StockDecreaRequestDto> listDto){
        //StockDecreaRequestDto를 반복하여 productId를 추출한다.
        List<UUID> productIdList = listDto.stream().
            map(StockDecreaRequestDto::getProductId).
            collect(Collectors.toList());

        // 재고 리스트를 가져온다. - 여기에 비관적 락이 걸림.
        List<Stock> stockList = stockRepository.findAllByProductIdInForUpdate(productIdList);
        // 재고 이력 리스트를 담을 새로운 리스트를 만든다.
        List<StockHistory> newStockHistory = new ArrayList<>();

        // 전달 받은 데이터를 map으로 변환한다.
        Map<UUID,StockDecreaRequestDto> mapDto = new  HashMap<>();
        for(StockDecreaRequestDto dto : listDto){
            mapDto.put(dto.getProductId(), dto);
        }

        //위에서 변환된 map과 동일한 Id를 가진 것과 비교해서, 비교 한다.
        for(Stock stock : stockList){
            // 현재 재고의 productId를 꺼내서, StockDecreaRequestDto 단일 객체를 꺼낸다.
            StockDecreaRequestDto dto = mapDto.get(stock.getProductId());
            StockHistory newHistory;
            //현재 재고 수량과 비교하여 현재 재고 수량이 더 많으면
            if(stock.getQuantity() >= dto.getQuantity()) {
                //재고를 주문한 수량 만큼 감소 시킨다.
                stock.decreaStock(dto.getQuantity());
                //변경된 현재 재고를 바탕으로 재고 이력을 생성한다.
                newHistory = StockHistory.createDecreaStock(stock,dto.getQuantity());
            } else { //만일 재고 수량보다 더 많은 경우,
                //재고 수량이 더 많다는 상태값을 넣은 재고 이력을 생성한다.
                newHistory = StockHistory.createOutOfStock(stock,dto.getQuantity());
            }
            //새롭게 넣은 재고 이력을 재고이력 리스트에 저장.
            newStockHistory.add(newHistory);
        }
        //변경된 재고 이력 리스트를 DB에 모두 저장.
        stockHistoryRepository.saveAll(newStockHistory);
        return newStockHistory;
    }

}
