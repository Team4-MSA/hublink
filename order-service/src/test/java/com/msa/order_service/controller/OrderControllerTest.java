package com.msa.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.response.paging.PageRes;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.MakeOrderDetailResDto;
import com.msa.order_service.dto.res.OrderDetailResDto;
import com.msa.order_service.dto.res.UserOrderResDto;
import com.msa.order_service.service.OrderService;
import com.msa.order_service.type.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private UUID userId;
    private UUID orderKey;
    private UUID orderId;
    private OrderMakeReqDto orderMakeReqDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderKey = UUID.randomUUID();
        orderId = UUID.randomUUID();

        OrderMakeReqDto.Items item = new OrderMakeReqDto.Items(UUID.randomUUID(), 5);
        orderMakeReqDto = OrderMakeReqDto.builder()
                .supplierCompanyId(UUID.randomUUID())
                .receiverCompanyId(UUID.randomUUID())
                .requestMemo("안전 배송 요망")
                .requestedDeliveryDeadline(LocalDateTime.now().plusDays(2))
                .items(List.of(item))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/orders - 사용자 주문 목록 페이징 조회 성공")
    void getOrders_Success() throws Exception {
        // given
        UserOrderResDto mockResDto = UserOrderResDto.builder()
                .orderId(orderId)
                .orderedByUserId(userId)
                .orderedByUserName("홍길동")
                .totalPrice(5000)
                .status(Status.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Page<UserOrderResDto> mockPage = new PageImpl<>(List.of(mockResDto), PageRequest.of(0, 10), 1);
        PageRes<UserOrderResDto> mockPageRes = new PageRes<>(mockPage);

        when(orderService.getOrders(eq(userId), eq(Status.CREATED), any(Pageable.class)))
                .thenReturn(mockPageRes);

        // when & then
        mockMvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].totalPrice").value(5000))
                .andExpect(jsonPath("$.data.content[0].status").value("CREATED"));

        verify(orderService, times(1)).getOrders(eq(userId), eq(Status.CREATED), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 성공 (201 CREATED)")
    void makeOrders_Success() throws Exception {
        // given
        // 해결책 ② 적용: Record 규격에 맞게 10개의 파라미터를 모두 세팅
        MakeOrderDetailResDto mockResponse = new MakeOrderDetailResDto(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                Status.CREATED,
                7500,
                "안전 배송 요망",
                LocalDateTime.now().plusDays(2),
                new ArrayList<>(),
                LocalDateTime.now()
        );

        when(orderService.makeOrders(any(OrderMakeReqDto.class), eq(userId), eq(orderKey)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .header("X-Order-Key", orderKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderMakeReqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.totalPrice").value(7500));

        verify(orderService, times(1)).makeOrders(any(OrderMakeReqDto.class), eq(userId), eq(orderKey));
    }

    @Test
    @DisplayName("GET /api/v1/orders/received - 업체 기준 받은 주문 목록 페이징 조회 성공")
    void getReceivedOrders_Success() throws Exception {
        // given
        UserOrderResDto mockResDto = UserOrderResDto.builder()
                .orderId(orderId)
                .orderedByUserId(userId)
                .orderedByUserName("공급사 직원")
                .totalPrice(10000)
                .status(Status.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Page<UserOrderResDto> mockPage = new PageImpl<>(List.of(mockResDto), PageRequest.of(0, 10), 1);
        PageRes<UserOrderResDto> mockPageRes = new PageRes<>(mockPage);

        when(orderService.getReceivedOrders(eq(userId), eq(Status.CREATED), any(Pageable.class)))
                .thenReturn(mockPageRes);

        mockMvc.perform(get("/api/v1/orders/received")
                        .header("X-User-Id", userId.toString())
                        .param("status", "CREATED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].totalPrice").value(10000));

        verify(orderService, times(1)).getReceivedOrders(eq(userId), eq(Status.CREATED), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 단건 상세 조회 성공")
    void getOrderById_Success() throws Exception {
        // given
        OrderDetailResDto mockDetail = OrderDetailResDto.builder()
                .orderId(orderId)
                .status(Status.CREATED.name())
                .totalPrice(3500)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(mockDetail);

        // when & then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalPrice").value(3500));

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/cancel - 주문 취소 성공")
    void cancelOrder_Success() throws Exception {
        // given
        doNothing().when(orderService).cancelOrder(orderId);

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderService, times(1)).cancelOrder(orderId);
    }
}