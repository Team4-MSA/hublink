package com.msa.order_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.MakeOrderDetailResDto;
import com.msa.order_service.dto.res.OrderDetailResDto;
import com.msa.order_service.dto.res.UserOrderResDto;
import com.msa.order_service.service.OrderService;
import com.msa.order_service.type.Status;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 목록 조회(사용자)", description = "로그인 사용자가 속한 수령업체의 주문 내역 조회")
    @GetMapping
    public PageRes<UserOrderResDto> getOrders(@RequestHeader("X-User-Id") UUID userId, @RequestParam(required = false) Status status, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getOrders(userId, status, pageable);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "주문 생성", description = "수령업체가 공급업체의 상품을 주문")
    @PostMapping
    public MakeOrderDetailResDto makeOrders(@RequestBody @Valid OrderMakeReqDto orderMakeReqDto, @RequestHeader("X-User-Id") UUID userId, @RequestHeader("X-Order-Key") UUID orderKey) {
        return orderService.makeOrders(orderMakeReqDto, userId, orderKey);
    }

    @Operation(summary = "주문 목록 조회(업체)", description = "로그인 사용자가 속한 공급업체가 받은 주문 목록 조회")
    @GetMapping("/received")
    public PageRes<UserOrderResDto> getReceivedOrders(@RequestHeader("X-User-Id") UUID userId, @RequestParam(required = false) Status status, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getReceivedOrders(userId, status, pageable);
    }

    @Operation(summary = "주문 상세 조회", description = "특정 주문의 기본 정보와 주문 상품 목록을 조회")
    @GetMapping("/{orderId}")
    public OrderDetailResDto getOrderById(@PathVariable("orderId") UUID orderId) {
        return orderService.getOrderById(orderId);
    }

    @Operation(summary = "주문 취소", description = "생성된 주문을 취소")
    @PatchMapping("/{orderId}/cancel")
    public void cancelOrder(@PathVariable("orderId") UUID orderId) {
        orderService.cancelOrder(orderId);
    }

}
