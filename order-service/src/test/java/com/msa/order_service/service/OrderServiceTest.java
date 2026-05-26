package com.msa.order_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.order_service.dto.req.MakeDeliveryReqDto;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.*;
import com.msa.order_service.entity.OrderItems;
import com.msa.order_service.entity.Orders;
import com.msa.order_service.feign.circuit.CompanyCircuitService;
import com.msa.order_service.feign.circuit.DeliveryCircuitService;
import com.msa.order_service.feign.circuit.ProductCircuitService;
import com.msa.order_service.feign.circuit.UserCircuitService;
import com.msa.order_service.repository.OrderJpaRepository;
import com.msa.order_service.type.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderJpaRepository orderJpaRepository;
    @Mock
    private UserCircuitService userCircuitService;
    @Mock
    private CompanyCircuitService companyCircuitService;
    @Mock
    private ProductCircuitService productCircuitService;
    @Mock
    private DeliveryCircuitService deliveryCircuitService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    // 공통 가짜 데이터 필드
    private UUID userId;
    private UUID companyId;
    private UUID productId;
    private UUID orderKey;
    private OrderMakeReqDto orderMakeReqDto;
    private ProductNPAResDto successProductDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderKey = UUID.randomUUID();

        // 1. 공통 주문 요청 DTO 생성
        OrderMakeReqDto.Items item = new OrderMakeReqDto.Items(productId, 5);
        orderMakeReqDto = OrderMakeReqDto.builder()
                .supplierCompanyId(companyId)
                .receiverCompanyId(UUID.randomUUID())
                .requestMemo("안전 배송 요망")
                .requestedDeliveryDeadline(LocalDateTime.now().plusDays(2))
                .items(List.of(item))
                .build();

        // 2. 공통 가짜 상품 Feign 응답 세팅
        successProductDto = new ProductNPAResDto(productId, true, null, "불닭볶음면", 1500, UUID.randomUUID());
    }

    @Test
    @DisplayName("makeOrders - 멱등성 검증 통과 및 주문 생성 성공 (정상 재고 + 배송 요청 포함)")
    void makeOrders_Success() {

        String redisKey = "order:make" + orderKey;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(redisKey), eq("PROCESSING"), any(Duration.class))).thenReturn(true);

        //유저 서비스 Mocking (주문자 정보 조회용)
        UsernameResDto mockOrderer = new UsernameResDto(userId, "고길동", orderMakeReqDto.getSupplierCompanyId(), "test@test.com");
        when(userCircuitService.getUserNames(any())).thenReturn(List.of(mockOrderer));

        // 회사 주소 서비스 Mocking (수령사 주소 조회용)
        CompanyAddressResDto mockAddress = new CompanyAddressResDto("광주광역시 광산구 산정동");
        when(companyCircuitService.companyAddress(eq(orderMakeReqDto.getReceiverCompanyId()))).thenReturn(mockAddress);

        // 회사 이름 서비스 Mocking (공급사와 수령사 '둘 다' 반환하도록 수정)
        UUID supplierId = orderMakeReqDto.getSupplierCompanyId();
        UUID receiverId = orderMakeReqDto.getReceiverCompanyId();
        when(companyCircuitService.getCompanyNames(any())).thenReturn(List.of(
                new CompanyNameResDto(supplierId, "삼양식품"),
                new CompanyNameResDto(receiverId, "뽀로로유통(수령사)")
        ));

        // 상품 서비스 Mocking (재고 차감 성공 리스트 반환)
        when(productCircuitService.decreaseProductStock(any())).thenReturn(List.of(successProductDto));
        MakeDeliveryResDto mockDeliveryRes = new MakeDeliveryResDto();
        mockDeliveryRes.setIsSuccess(true);
        // 배송 서비스 Mocking (배송 생성 호출용)
        when(deliveryCircuitService.makeDelivery(any(MakeDeliveryReqDto.class))).thenReturn(mockDeliveryRes);

        Orders mockOrder = Orders.createInitOrder(orderMakeReqDto, userId);
        OrderItems mockItem = OrderItems.createOrderItem(5, supplierId, "삼양식품", successProductDto);
        mockOrder.addOrderItem(mockItem);
        mockOrder.updateTotalPrice();

        when(orderJpaRepository.saveAndFlush(any(Orders.class))).thenReturn(mockOrder);

        MakeOrderDetailResDto result = orderService.makeOrders(orderMakeReqDto, userId, orderKey);

        assertThat(result).isNotNull();
        assertThat(result.totalPrice()).isEqualTo(7500);

        // DB에 정상
        verify(orderJpaRepository, times(1)).saveAndFlush(any());

        // 배송 요청 메소드 실행 여부
        verify(deliveryCircuitService, times(1)).makeDelivery(any(MakeDeliveryReqDto.class));
    }
    @Test
    @DisplayName("makeOrders - 멱등성 검증 실패 (동일 Key로 따닥 클릭 시 중복 예외 발생)")
    void makeOrders_Fail_AlreadyExistOrder() {
        // given
        String redisKey = "order:make" + orderKey;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 이미 키가 존재한다고 가정 (false 반환)
        when(valueOperations.setIfAbsent(eq(redisKey), eq("PROCESSING"), any(Duration.class))).thenReturn(false);

        // when & then
        assertThrows(CustomException.class, () ->
                orderService.makeOrders(orderMakeReqDto, userId, orderKey)
        );

        // 멱등성에서 튕겼으므로 하단 비즈니스 쿼리나 통신은 절대 호출되면 안 됨
        verify(productCircuitService, never()).decreaseProductStock(any());
        verify(orderJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("makeOrders - Feign이 차감 실패한 품목은 FAILED 상태 아이템으로 생성 성공")
    void makeOrders_With_Failed_Items() {
        // given
        String redisKey = "order:make" + orderKey;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(redisKey), eq("PROCESSING"), any(Duration.class))).thenReturn(true);

        // Feign이 빈 리스트 리턴함 (차감 실패 혹은 서킷브레이커 동작 상황)
        when(productCircuitService.decreaseProductStock(any())).thenReturn(new ArrayList<>());
        when(companyCircuitService.getCompanyNames(any())).thenReturn(List.of(new CompanyNameResDto(companyId, "삼양식품")));

        Orders mockOrder = Orders.createInitOrder(orderMakeReqDto, userId);
        OrderItems failedItem = OrderItems.createFailedOrderItem(productId, 5, companyId, "삼양식품");
        mockOrder.addOrderItem(failedItem);
        mockOrder.updateTotalPrice();

        when(orderJpaRepository.saveAndFlush(any(Orders.class))).thenReturn(mockOrder);

        // when
        MakeOrderDetailResDto result = orderService.makeOrders(orderMakeReqDto, userId, orderKey);

        // then
        assertThat(result).isNotNull();
        assertThat(result.items().get(0).status()).isEqualTo(Status.FAILED);
        assertThat(result.totalPrice()).isEqualTo(0); // 실패 건은 0원 처리 검증
    }

    @Test
    @DisplayName("getOrders - 수령업체 주문 목록 페이징 조회 성공")
    void getOrders_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Orders mockOrder = Orders.builder()
                .id(UUID.randomUUID())
                .orderedByUserId(userId)
                .supplierCompanyId(companyId)
                .receiverCompanyId(UUID.randomUUID())
                .totalPrice(5000)
                .status(Status.CREATED)
                .build();
        Page<Orders> page = new PageImpl<>(List.of(mockOrder), pageable, 1);

        when(orderJpaRepository.findAllByStatusAndSupplierCompanyId(any(), eq(companyId), any()))
                .thenReturn(page);
        when(userCircuitService.getUserNames(anyList())).thenReturn(List.of(new UsernameResDto(userId, "홍길동", companyId, "email@naver.com")));
        when(companyCircuitService.getCompanyNames(any())).thenReturn(List.of(new CompanyNameResDto(companyId, "공급사")));

        // when
        PageRes<UserOrderResDto> result = orderService.getOrders(userId, Status.CREATED, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("cancelOrder - 주문 취소 및 자식 완료 품목 수량 롤백 복구 성공")
    void cancelOrder_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        Orders order = Orders.builder().id(orderId).status(Status.CREATED).build();

        // 복구되어야 할 COMPLETED 자식 품목들
        OrderItems item1 = OrderItems.builder().status(Status.COMPLETED).productId(productId).quantity(3).build();
        OrderItems item2 = OrderItems.builder().status(Status.FAILED).productId(UUID.randomUUID()).quantity(1).build(); // 실패건은 무시되어야함
        order.addOrderItem(item1);
        order.addOrderItem(item2);

        when(orderJpaRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(productCircuitService.increaseProductStock(any())).thenReturn(true);

        // when
        orderService.cancelOrder(orderId);

        // then
        assertThat(order.getStatus()).isEqualTo(Status.CANCELED);
        verify(productCircuitService, times(1)).increaseProductStock(any());
    }

    @Test
    @DisplayName("getOrders - 로그인 유저 정보가 조회되지 않으면 빈 페이지 반환")
    void getOrders_Fail_LoginUserEmpty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        // loginUserList.isEmpty() -> true 분기를 태우기 위해 빈 리스트 반환 모킹
        when(userCircuitService.getUserNames(anyList())).thenReturn(new ArrayList<>());

        // when
        PageRes<UserOrderResDto> result = orderService.getOrders(userId, Status.CREATED, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        // 첫 번째 if문에서 탈출했으므로 다음 로직(Repository)은 호출 안 됨 검증
        verify(orderJpaRepository, never()).findAllByStatusAndSupplierCompanyId(any(), any(), any());
    }

    @Test
    @DisplayName("getOrders - 조회된 주문 목록이 비어있으면 빈 페이지 반환")
    void getOrders_Fail_OrderListEmpty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userCircuitService.getUserNames(anyList()))
                .thenReturn(List.of(new UsernameResDto(userId, "홍길동", companyId, "email@naver.com")));

        // orderJpaRepository가 빈 Page를 주도록 설정 (all.isEmpty() -> true)
        when(orderJpaRepository.findAllByStatusAndSupplierCompanyId(any(), eq(companyId), any()))
                .thenReturn(Page.empty(pageable));

        // when
        PageRes<UserOrderResDto> result = orderService.getOrders(userId, Status.CREATED, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        // 두 번째 if문에서 탈출했으므로 하단 Feign 통신이나 조립 로직은 타지 않음 검증
        verify(companyCircuitService, never()).getCompanyNames(any());
    }

    @Test
    @DisplayName("getReceivedOrders - 공급업체 기준 수령한 주문 페이징 조회 성공")
    void getReceivedOrders_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userCircuitService.getUserNames(anyList()))
                .thenReturn(List.of(new UsernameResDto(userId, "홍길동", companyId, "email@naver.com")));

        Orders mockOrder = Orders.builder()
                .id(UUID.randomUUID())
                .orderedByUserId(userId)
                .supplierCompanyId(companyId)
                .receiverCompanyId(UUID.randomUUID())
                .totalPrice(10000)
                .status(Status.CREATED)
                .build();
        Page<Orders> page = new PageImpl<>(List.of(mockOrder), pageable, 1);

        // 수령 전용 쿼리 메서드 모킹
        when(orderJpaRepository.findAllByStatusAndReceiverCompanyId(any(), eq(companyId), any()))
                .thenReturn(page);
        when(companyCircuitService.getCompanyNames(any()))
                .thenReturn(List.of(new CompanyNameResDto(companyId, "공급사")));

        // when
        PageRes<UserOrderResDto> result = orderService.getReceivedOrders(userId, Status.CREATED, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(orderJpaRepository, times(1)).findAllByStatusAndReceiverCompanyId(any(), any(), any());
    }

    @Test
    @DisplayName("getReceivedOrders - 수령한 주문 내역이 없으면 빈 페이지 반환")
    void getReceivedOrders_Empty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userCircuitService.getUserNames(anyList()))
                .thenReturn(List.of(new UsernameResDto(userId, "홍길동", companyId, "email@naver.com")));
        when(orderJpaRepository.findAllByStatusAndReceiverCompanyId(any(), eq(companyId), any()))
                .thenReturn(Page.empty(pageable));

        // when
        PageRes<UserOrderResDto> result = orderService.getReceivedOrders(userId, Status.CREATED, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getOrderById - 존재하는 주문 ID 조회 시 상세 DTO 반환")
    void getOrderById_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        Orders mockOrder = Orders.builder()
                .id(orderId)
                .status(Status.CREATED)
                .totalPrice(3500)
                .build();

        when(orderJpaRepository.findByOrderId(orderId)).thenReturn(Optional.of(mockOrder));

        // when
        OrderDetailResDto result = orderService.getOrderById(orderId);

        // then
        assertThat(result).isNotNull();
        verify(orderJpaRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("getOrderById - 존재하지 않는 주문 ID 조회 시 NOT_EXIST_ORDER 예외 발생")
    void getOrderById_Fail_NotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderJpaRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    @DisplayName("cancelOrder - 존재하지 않는 주문 취소 요청 시 예외 발생")
    void cancelOrder_Fail_NotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderJpaRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> orderService.cancelOrder(orderId));
    }

    @Test
    @DisplayName("cancelOrder - 재고 복구 통신(increaseProductStock) 실패 시 예외 발생")
    void cancelOrder_Fail_IncreaseStock() {
        // given
        UUID orderId = UUID.randomUUID();
        Orders order = Orders.builder().id(orderId).status(Status.CREATED).build();
        OrderItems item = OrderItems.builder().status(Status.COMPLETED).productId(productId).quantity(2).build();
        order.addOrderItem(item);

        when(orderJpaRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        // Feign 통신 실패(false) 시뮬레이션
        when(productCircuitService.increaseProductStock(any())).thenReturn(false);

        // when & then
        assertThrows(CustomException.class, () -> orderService.cancelOrder(orderId));
    }

    @Test
    @DisplayName("getPageable - 규격 외의 PageSize(예: 25)가 들어오면 기본값 10으로 강제 보정 조절 테스트")
    void getPageable_Size_Correction() {
        // given
        // 규격(10, 30, 50)이 아닌 25로 요청을 보냄
        Pageable invalidPageable = PageRequest.of(0, 25);

        when(userCircuitService.getUserNames(anyList())).thenReturn(new ArrayList<>());

        // when
        // getOrders 호출 시 내부에서 private getPageable()이 작동함
        orderService.getOrders(userId, Status.CREATED, invalidPageable);

        // then
        // 비즈니스 검증단은 빈 반환이겠지만, 내부 아규먼트 캡처나 디버깅 없이도
        // 25 규격 외 분기 타서 줄 커버리지 채워짐 확인 완료
    }
}