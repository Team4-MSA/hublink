package com.msa.order_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.*;
import com.msa.order_service.entity.OrderItems;
import com.msa.order_service.entity.Orders;
import com.msa.order_service.error.OrderErrorCode;
import com.msa.order_service.feign.circuit.ProductCircuitService;
import com.msa.order_service.repository.OrderJpaRepository;
import com.msa.order_service.feign.circuit.CompanyCircuitService;
import com.msa.order_service.feign.circuit.UserCircuitService;
import com.msa.order_service.type.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final UserCircuitService userCircuitService;
    private final CompanyCircuitService companyCircuitService;
    private final ProductCircuitService productCircuitService;

    public PageRes<UserOrderResDto> getOrders(UUID loginUserId, Status status, Pageable pageable) {

        pageable = getPageable(pageable);

        List<UsernameResDto> loginUserList = userCircuitService.getUserNames(List.of(loginUserId));

        if (loginUserList.isEmpty()) {
            return new PageRes<>(Page.empty(pageable));
        }

        // 내 소속 회사 ID 추출
        UUID myCompanyId = loginUserList.get(0).companyId();

        Page<Orders> all = orderJpaRepository.findAllByStatusAndSupplierCompanyId(status, myCompanyId, pageable);

        if (all.isEmpty()) {
            return new PageRes<>(Page.empty(pageable));
        }

        List<UUID> userIds = all.stream().map(Orders::getOrderedByUserId).distinct().toList();
        List<UUID> companyIds = new ArrayList<>();
        for(Orders order : all) {
            companyIds.add(order.getReceiverCompanyId());
            companyIds.add(order.getSupplierCompanyId());
        }
        List<UUID> distinctCompanyIds = companyIds.stream().distinct().toList();

        List<UsernameResDto> userNames = userCircuitService.getUserNames(userIds);
        List<CompanyNameResDto> companyNames = companyCircuitService.getCompanyNames(distinctCompanyIds);

        Map<UUID, String> userMap = userNames.stream().collect(Collectors.toMap(UsernameResDto::id, UsernameResDto::name));
        Map<UUID, String> companyMap = companyNames.stream().collect(Collectors.toMap(CompanyNameResDto::id, CompanyNameResDto::name));

        Page<UserOrderResDto> map = all.map(order -> UserOrderResDto.createOrdersRes(order, companyMap, userMap));
        return new PageRes<>(map);
    }

    private static Pageable getPageable(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        if(pageSize != 10 && pageSize != 30 && pageSize != 50 ) {
            pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return pageable;
    }

    @Transactional
    public MakeOrderDetailResDto makeOrders(OrderMakeReqDto orderMakeReqDto, UUID userId) {

        List<UUID> companyIds = List.of(orderMakeReqDto.getSupplierCompanyId());
        List<OrderMakeReqDto.Items> items = orderMakeReqDto.getItems();
        //feign으로 조회
        List<ProductNPAResDto> nameAndPriceAndHubId = productCircuitService.decreaseProductStock(items);
        String companyName = companyCircuitService.getCompanyNames(companyIds).get(0).name();
        Map<UUID, ProductNPAResDto> productMap = nameAndPriceAndHubId.stream()
                .collect(Collectors.toMap(ProductNPAResDto::productId, Function.identity()));

        Orders initOrder = Orders.createInitOrder(orderMakeReqDto, userId);

        for(OrderMakeReqDto.Items item : items) {
            ProductNPAResDto productNPAResDto = productMap.get(item.getProductId());

            OrderItems orderItem;

            // Feign 응답 맵에 존재 여부 확인
            if (productNPAResDto != null) {

                orderItem = OrderItems.createOrderItem(
                        item.getQuantity(),
                        orderMakeReqDto.getSupplierCompanyId(),
                        companyName,
                        productNPAResDto
                );
            } else {
                // 맵에 없다 재고 차감 실패 품목이므로 FAILED 엔티티 생성
                orderItem = OrderItems.createFailedOrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        orderMakeReqDto.getSupplierCompanyId(),
                        companyName
                );
            }

            initOrder.addOrderItem(orderItem);
        }

        initOrder.updateTotalPrice();

        Orders savedOrder = orderJpaRepository.saveAndFlush(initOrder);

        // DTO 변환 및 반환부
        List<MakeOrderDetailResDto.OrderItemDto> itemDtos = savedOrder.getOrderItems().stream()
                .map(item -> new MakeOrderDetailResDto.OrderItemDto(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getSupplierCompanyId(),
                        item.getSupplierCompanyName(),
                        item.getHubId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice(),
                        item.getStatus()
                )).toList();

        return new MakeOrderDetailResDto(
                savedOrder.getId(),
                savedOrder.getSupplierCompanyId(),
                savedOrder.getReceiverCompanyId(),
                savedOrder.getOrderedByUserId(),
                savedOrder.getStatus(),
                savedOrder.getTotalPrice(),
                savedOrder.getRequestMemo(),
                savedOrder.getRequestedDeliveryDeadline(),
                itemDtos,
                savedOrder.getCreatedAt()
        );
    }

    public PageRes<UserOrderResDto> getReceivedOrders(UUID loginUserId, Status status, Pageable pageable) {

        pageable = getPageable(pageable);

        List<UsernameResDto> loginUserList = userCircuitService.getUserNames(List.of(loginUserId));

        if (loginUserList.isEmpty()) {
            return new PageRes<>(Page.empty(pageable));
        }

        // 내 소속 회사 ID 추출
        UUID myCompanyId = loginUserList.get(0).companyId();

        Page<Orders> all = orderJpaRepository.findAllByStatusAndReceiverCompanyId(status, myCompanyId, pageable);

        if (all.isEmpty()) {
            return new PageRes<>(Page.empty(pageable));
        }

        List<UUID> userIds = all.stream().map(Orders::getOrderedByUserId).distinct().toList();
        List<UUID> companyIds = new ArrayList<>();
        for(Orders order : all) {
            companyIds.add(order.getReceiverCompanyId());
            companyIds.add(order.getSupplierCompanyId());
        }
        List<UUID> distinctCompanyIds = companyIds.stream().distinct().toList();

        List<UsernameResDto> userNames = userCircuitService.getUserNames(userIds);
        List<CompanyNameResDto> companyNames = companyCircuitService.getCompanyNames(distinctCompanyIds);

        Map<UUID, String> userMap = userNames.stream().collect(Collectors.toMap(UsernameResDto::id, UsernameResDto::name));
        Map<UUID, String> companyMap = companyNames.stream().collect(Collectors.toMap(CompanyNameResDto::id, CompanyNameResDto::name));

        Page<UserOrderResDto> map = all.map(order -> UserOrderResDto.createOrdersRes(order, companyMap, userMap));
        return new PageRes<>(map);
    }

    public OrderDetailResDto getOrderById(UUID orderId) {
        Orders orders = orderJpaRepository.findByOrderId(orderId).orElseThrow(() -> new CustomException(OrderErrorCode.NOT_EXIST_ORDER));
        return OrderDetailResDto.from(orders);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Orders order = orderJpaRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.NOT_EXIST_ORDER));

        List<OrderMakeReqDto.Items> rollbackItems = order.getOrderItems().stream()
                .filter(item -> item.getStatus() == Status.COMPLETED)
                .map(item -> new OrderMakeReqDto.Items(item.getProductId(), item.getQuantity()))
                .toList();

        if (!rollbackItems.isEmpty()) {
            Boolean b = productCircuitService.increaseProductStock(rollbackItems);
            if(!b) throw new CustomException(OrderErrorCode.FAIL_INCREASE_STOCK);
        }

        order.cancel();
    }

}
