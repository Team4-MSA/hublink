package com.msa.stock_service.controller;

import com.msa.stock_service.dto.StockDecreaRequestDto;
import com.msa.stock_service.dto.StockHistoryResponseDto;
import com.msa.stock_service.dto.StockRequestDto;
import com.msa.stock_service.dto.StockResponseDto;
import com.msa.stock_service.entity.Stock;
import com.msa.stock_service.entity.StockHistory;
import com.msa.stock_service.service.StockOrchestrator;
import com.msa.stock_service.service.StockService;
import jakarta.ws.rs.PATCH;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
public class StockController {
    private final StockService stockService;
    private final StockOrchestrator stockOrchestrator;

    /**
     * 재고 생성
     * @param dto
     * @return
     */
    @PostMapping
    public StockResponseDto createStock(StockRequestDto dto){
        Stock newStock = stockService.createStock(dto);
        return StockResponseDto.from(newStock);
    }

    /**
     * 재고 조회 -> 재고 변경 -> 새로운 재고 이력 생성
     * @param listDto
     * @return
     */
    @PostMapping("/decreae")
    public List<StockHistoryResponseDto> decreaStock(List<StockDecreaRequestDto> listDto){
        return stockOrchestrator.decreaStock(listDto);
    }
}
