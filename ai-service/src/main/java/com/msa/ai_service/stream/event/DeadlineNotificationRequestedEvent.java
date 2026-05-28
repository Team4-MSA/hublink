package com.msa.ai_service.stream.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadlineNotificationRequestedEvent {
    @NotNull(message = "이벤트 ID는 필수입니다.")
    private UUID eventId;

    @NotNull(message = "배송 ID는 필수입니다.")
    private UUID deliveryId;

    @NotNull(message = "주문 ID는 필수입니다.")
    private UUID orderId;

    @NotBlank(message = "주문자 이름은 필수입니다.")
    private String ordererName;

    @NotBlank(message = "주문자 이메일은 필수입니다.")
    private String ordererEmail;

    @NotNull(message = "주문 시간은 필수입니다.")
    private LocalDateTime orderedAt;

    private String requestMessage;

    @NotNull(message = "수신자 사용자 ID는 필수입니다.")
    private UUID receiverUserId;

    @NotBlank(message = "수신자 Slack ID는 필수입니다.")
    private String receiverSlackId;

    @NotEmpty(message = "상품 목록은 필수입니다.")
    @Valid
    private List<ProductInfo> products;

    @NotNull(message = "요청 도착 시각은 필수입니다.")
    private LocalDateTime requestedArrivalAt;

    @NotBlank(message = "발송지 허브명은 필수입니다.")
    private String departureHubName;

    @NotBlank(message = "도착지 주소는 필수입니다.")
    private String destinationAddress;

    @NotBlank(message = "배송 담당자 이름은 필수입니다.")
    private String deliveryManagerName;

    @NotBlank(message = "배송 담당자 이메일은 필수입니다.")
    private String deliveryManagerEmail;

    @NotEmpty(message = "배송 경로 정보는 필수입니다.")
    @Valid
    private List<RouteInfo> routeInfo;

    @NotNull(message = "근무 시작 시간은 필수입니다.")
    private LocalTime workStartTime;

    @NotNull(message = "근무 종료 시간은 필수입니다.")
    private LocalTime workEndTime;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductInfo {

        @NotBlank(message = "상품명은 필수입니다.")
        private String productName;

        @NotNull(message = "상품 수량은 필수입니다.")
        @Positive(message = "상품 수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RouteInfo {

        @NotBlank(message = "출발 허브명은 필수입니다.")
        private String departureHubName;

        private String arrivalHubName;
        private String arrivalCompanyName;

        @NotNull(message = "예상 이동 거리는 필수입니다.")
        @PositiveOrZero(message = "예상 이동 거리는 0 이상이어야 합니다.")
        private Double estimatedDistanceKm;

        @NotNull(message = "예상 소요 시간은 필수입니다.")
        @Positive(message = "예상 소요 시간은 1분 이상이어야 합니다.")
        private Integer estimatedDurationMin;

        @NotBlank(message = "경로 유형은 필수입니다.")
        private String routeType;
    }
}