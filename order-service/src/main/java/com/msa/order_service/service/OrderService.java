package com.msa.order_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.order_service.dto.req.MakeDeliveryReqDto;
import com.msa.order_service.dto.req.ModifyStockReqDto;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.*;
import com.msa.order_service.entity.OrderItems;
import com.msa.order_service.entity.Orders;
import com.msa.order_service.entity.Outbox;
import com.msa.order_service.error.OrderErrorCode;
import com.msa.order_service.feign.circuit.DeliveryCircuitService;
import com.msa.order_service.feign.circuit.ProductCircuitService;
import com.msa.order_service.repository.OrderJpaRepository;
import com.msa.order_service.feign.circuit.CompanyCircuitService;
import com.msa.order_service.feign.circuit.UserCircuitService;
import com.msa.order_service.repository.OutboxRepository;
import com.msa.order_service.type.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final UserCircuitService userCircuitService;
    private final CompanyCircuitService companyCircuitService;
    private final ProductCircuitService productCircuitService;
    private final DeliveryCircuitService deliveryCircuitService;
    private final RedisTemplate<String, String> redisTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

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
    public void publishDecreaseStockEvent(UUID orderId, ModifyStockReqDto items) {
        try{
            String jsonPayload = objectMapper.writeValueAsString(items);

            Outbox stockOutbox = Outbox.builder()
                    .aggregateType("STOCK")
                    .aggregateId(orderId.toString())
                    .topic("stock.decrease")
                    .payload(jsonPayload)
                    .processed(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(stockOutbox);
        }catch (Exception e) {
            log.error("재고 차감 직렬화 실패", e);
            throw new CustomException(OrderErrorCode.FAIL_OUTBOX);
        }
    }

    @Transactional
    public void publishMakeDeliveryEvent(MakeDeliveryReqDto makeDeliveryReqDto) {
        try{
            String jsonPayload = objectMapper.writeValueAsString(makeDeliveryReqDto);

            Outbox stockOutbox = Outbox.builder()
                    .aggregateType("DELIVERY")
                    .aggregateId(makeDeliveryReqDto.getOrderId().toString())
                    .topic("delivery.create")
                    .payload(jsonPayload)
                    .processed(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(stockOutbox);
        }catch (Exception e) {
            log.error("재고 차감 직렬화 실패", e);
            throw new CustomException(OrderErrorCode.FAIL_OUTBOX);
        }
    }

//    @Transactional
//    public MakeOrderDetailResDto makeOrders(OrderMakeReqDto orderMakeReqDto, UUID userId, UUID orderKey) {
//
//        String redisKey = "order:make" + orderKey;
//        Boolean b = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(1));
//
//        if (Boolean.FALSE.equals(b)) {
//            throw new CustomException(OrderErrorCode.ALREADY_EXIST_ORDER);
//        }
//
//        // 1. 단순 조회성 외부 호출 (실패해도 원상복구 필요 없음)
//        List<UsernameResDto> ordererInfoList = userCircuitService.getUserNames(List.of(userId));
//        UsernameResDto orderer = ordererInfoList.isEmpty() ?
//                new UsernameResDto(userId, "알 수 없는 유저", null, "unknown@email.com") : ordererInfoList.get(0);
//
//        CompanyAddressResDto addressRes = companyCircuitService.companyAddress(orderMakeReqDto.getReceiverCompanyId());
//        String deliveryAddress = (addressRes != null) ? addressRes.getAddress() : "조회실패";
//
//        List<UUID> companyIds = List.of(orderMakeReqDto.getSupplierCompanyId(), orderMakeReqDto.getReceiverCompanyId());
//        List<OrderMakeReqDto.Items> items = orderMakeReqDto.getItems();
//
//        // 2. [상태 변경 시작] 외부 재고 차감 실행
//        List<ProductNPAResDto> nameAndPriceAndHubId = productCircuitService.decreaseProductStock(items);
//
//        // 재고 차감이 성공했음을 알리는 플래그 세팅
//        boolean isStockDecreased = true;
//
//        try { //재고는 깎였는데 이후 내 로직이나 DB 저장 중 터지는 경우 방어
//
//            // 회사 이름 및 상품 맵 조립
//            List<CompanyNameResDto> companyNameResDtos = companyCircuitService.getCompanyNames(companyIds);
//            Map<UUID, String> companyNameMap = companyNameResDtos.stream()
//                    .collect(Collectors.toMap(CompanyNameResDto::id, CompanyNameResDto::name));
//
//            String supplierCompanyName = companyNameMap.getOrDefault(orderMakeReqDto.getSupplierCompanyId(), "알 수 없는 공급사");
//            String receiverCompanyName = companyNameMap.getOrDefault(orderMakeReqDto.getReceiverCompanyId(), "알 수 없는 수령사");
//
//            Map<UUID, ProductNPAResDto> productMap = nameAndPriceAndHubId.stream()
//                    .collect(Collectors.toMap(ProductNPAResDto::productId, Function.identity()));
//
//            Orders initOrder = Orders.createInitOrder(orderMakeReqDto, userId);
//
//            for(OrderMakeReqDto.Items item : items) {
//                ProductNPAResDto productNPAResDto = productMap.get(item.getProductId());
//                OrderItems orderItem;
//
//                if (productNPAResDto != null) {
//                    orderItem = OrderItems.createOrderItem(
//                            item.getQuantity(),
//                            orderMakeReqDto.getSupplierCompanyId(),
//                            supplierCompanyName,
//                            productNPAResDto
//                    );
//                } else {
//                    orderItem = OrderItems.createFailedOrderItem(
//                            item.getProductId(),
//                            item.getQuantity(),
//                            orderMakeReqDto.getSupplierCompanyId(),
//                            supplierCompanyName
//                    );
//                }
//                initOrder.addOrderItem(orderItem);
//            }
//
//            initOrder.updateTotalPrice();
//
//            // 내 주문 DB에 선반영 (Id 발급 목적)
//            Orders savedOrder = orderJpaRepository.saveAndFlush(initOrder);
//
//            // 배송 생성 요청 DTO 조립
//            MakeDeliveryReqDto deliveryReqDto = MakeDeliveryReqDto.from(savedOrder, orderer, deliveryAddress, receiverCompanyName);
//
//            // 3. 정상 상품이 존재할 때만 배송 서비스 호출
//            if (!deliveryReqDto.getProducts().isEmpty()) {
//                try { //내 DB 저장도 끝났는데 오직 배송 API만 터진 경우 방어
//                    deliveryCircuitService.makeDelivery(deliveryReqDto);
//                } catch (Exception e) {
//                    log.error("[배송 실패 보상 로직] 배송 서비스 호출 실패로 인해 재고를 복구합니다. 원인: {}", e.getMessage());
//                    rollbackStock(items);
//                    throw new CustomException(OrderErrorCode.FAIL_DELIVERY); // 내 주문 DB도 롤백되도록 예외 토스
//                }
//            }
//
//            // 4. 최종 결과 DTO 반환부 (대박 성공 시나리오)
//            List<MakeOrderDetailResDto.OrderItemDto> itemDtos = savedOrder.getOrderItems().stream()
//                    .map(item -> new MakeOrderDetailResDto.OrderItemDto(
//                            item.getId(), item.getProductId(), item.getProductName(),
//                            item.getSupplierCompanyId(), item.getSupplierCompanyName(),
//                            item.getHubId(), item.getQuantity(), item.getUnitPrice(),
//                            item.getTotalPrice(), item.getStatus()
//                    )).toList();
//
//            return new MakeOrderDetailResDto(
//                    savedOrder.getId(), savedOrder.getSupplierCompanyId(), savedOrder.getReceiverCompanyId(),
//                    savedOrder.getOrderedByUserId(), savedOrder.getStatus(), savedOrder.getTotalPrice(),
//                    savedOrder.getRequestMemo(), savedOrder.getRequestedDeliveryDeadline(), itemDtos, savedOrder.getCreatedAt()
//            );
//
//        } catch (CustomException ce) {
//            // 배송 쪽에서 의도적으로 던진 CustomException은 그대로 통과시켜 상위 트랜잭션 롤백 유도
//            throw ce;
//        } catch (Exception e) {
//            // [주문 내부 로직 실패 방어] 맵 조립 에러나 saveAndFlush 등 주문 도중 터지면 재고 원상복구
//            log.error("[주문 내부 실패 보상 로직] 주문 처리 중 시스템 예외가 발생하여 재고를 복구합니다. 원인: {}", e.getMessage());
//            if (isStockDecreased) {
//                rollbackStock(items);
//            }
//            throw e; // 주문 DB 롤백 유도
//        }
//    }

    @Transactional
    public MakeOrderDetailResDto makeOrders(OrderMakeReqDto orderMakeReqDto, UUID userId, UUID orderKey) {

        // 레디스 중복 주문 방지
        String redisKey = "order:make" + orderKey;
        Boolean b = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(1));

        if (Boolean.FALSE.equals(b)) {
            throw new CustomException(OrderErrorCode.ALREADY_EXIST_ORDER);
        }

        // 주문 유저 정보 조회
        List<UsernameResDto> ordererInfoList = userCircuitService.getUserNames(List.of(userId));
        UsernameResDto orderer = ordererInfoList.isEmpty() ?
                new UsernameResDto(userId, "알 수 없는 유저", null, "unknown@email.com") : ordererInfoList.get(0);
        // 받는 회사 주소 조회
        CompanyAddressResDto addressRes = companyCircuitService.companyAddress(orderMakeReqDto.getReceiverCompanyId());
        String deliveryAddress = (addressRes != null) ? addressRes.getAddress() : "조회실패";

        List<UUID> companyIds = List.of(orderMakeReqDto.getSupplierCompanyId(), orderMakeReqDto.getReceiverCompanyId());
        List<OrderMakeReqDto.Items> items = orderMakeReqDto.getItems();

        // 재고 감소는 kafka 비동기로 처리
        // List<ProductNPAResDto> nameAndPriceAndHubId = productCircuitService.decreaseProductStock(items);

        // 3. 주문 초기 제공, 받음 회사명 조회
        List<CompanyNameResDto> companyNameResDtos = companyCircuitService.getCompanyNames(companyIds);
        Map<UUID, String> companyNameMap = companyNameResDtos.stream()
                .collect(Collectors.toMap(CompanyNameResDto::id, CompanyNameResDto::name));

        String supplierCompanyName = companyNameMap.getOrDefault(orderMakeReqDto.getSupplierCompanyId(), "알 수 없는 공급사");
        String receiverCompanyName = companyNameMap.getOrDefault(orderMakeReqDto.getReceiverCompanyId(), "알 수 없는 수령사");

        // 초기 Orders 생성 (PENDING)
        Orders initOrder = Orders.createInitOrder(orderMakeReqDto, userId);

        for(OrderMakeReqDto.Items item : items) {
            //초기 OrderItems 생성 (PENDING) -> 나머지 필드는 재고 차감 성공후 채워주기
            OrderItems orderItem = OrderItems.createPendingOrderItem(
                    item.getProductId(),
                    item.getQuantity(),
                    orderMakeReqDto.getSupplierCompanyId(),
                    supplierCompanyName
            );
            initOrder.addOrderItem(orderItem);
        }

        // 아직 단가를 모르니 총액은 0원 세팅 -> 재고 차감 성공후 채워주기
        initOrder.setZeroTotalPrice();

        // 내 주문 DB에 선반영
        Orders savedOrder = orderJpaRepository.saveAndFlush(initOrder);

        ModifyStockReqDto modifyStockReqDto = new ModifyStockReqDto(savedOrder.getId(), orderer.name(), orderer.email(), deliveryAddress, receiverCompanyName, items);
        // 재고 차감 Outbox 발행 메서드 호출
        publishDecreaseStockEvent(savedOrder.getId(), modifyStockReqDto);

        // 5. 최종 결과 DTO 반환부
        List<MakeOrderDetailResDto.OrderItemDto> itemDtos = savedOrder.getOrderItems().stream()
                .map(item -> new MakeOrderDetailResDto.OrderItemDto(
                        item.getId(), item.getProductId(), item.getProductName(),
                        item.getSupplierCompanyId(), item.getSupplierCompanyName(),
                        item.getHubId(), item.getQuantity(), item.getUnitPrice() == null ? 0 : item.getUnitPrice(),
                        item.getTotalPrice() == null ? 0 : item.getTotalPrice(), item.getStatus()
                )).toList();

        return new MakeOrderDetailResDto(
                savedOrder.getId(), savedOrder.getSupplierCompanyId(), savedOrder.getReceiverCompanyId(),
                savedOrder.getOrderedByUserId(), savedOrder.getStatus(), savedOrder.getTotalPrice(),
                savedOrder.getRequestMemo(), savedOrder.getRequestedDeliveryDeadline(), itemDtos, savedOrder.getCreatedAt()
        );

    }


    //재고 복구 전용 헬퍼 메서드
    private void rollbackStock(List<OrderMakeReqDto.Items> items) {
        try {
            productCircuitService.increaseProductStock(items);
            log.info("[보상 로직 완료] 차감되었던 상품 재고가 정상적으로 롤백되었습니다.");
        } catch (Exception re) {
            log.error("보상 로직인 재고 복구 API 실패하였습니다. 즉시 수동 데이터 보정이 필요합니다. 원인: {}", re.getMessage());
        }
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOutboxProcessed(UUID outboxId) {
        outboxRepository.findById(outboxId).ifPresent(Outbox::markProcessed);
    }

}
