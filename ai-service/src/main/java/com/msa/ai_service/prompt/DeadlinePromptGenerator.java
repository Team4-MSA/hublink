package com.msa.ai_service.prompt;

import com.msa.ai_service.stream.event.DeadlineNotificationRequestedEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class DeadlinePromptGenerator {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String generatePrompt(DeadlineNotificationRequestedEvent event) {

        String products = event.getProducts().stream()
                .map(product -> product.getProductName() + " " + product.getQuantity() + "개")
                .collect(Collectors.joining(", "));

        String routeInfo = event.getRouteInfo().stream()
                .map(route -> {
                    String arrivalName = route.getArrivalHubName() != null && !route.getArrivalHubName().isBlank()
                            ? route.getArrivalHubName() : route.getArrivalCompanyName();

                    return "- " + route.getDepartureHubName()
                            + " -> " + arrivalName
                            + ", 거리: " + route.getEstimatedDistanceKm() + "km"
                            + ", 예상 시간: " + route.getEstimatedDurationMin() + "분"
                            + ", 유형: " + route.getRouteType();
                })
                .collect(Collectors.joining("\n"));

        int totalDuration = event.getRouteInfo().stream()
                .mapToInt(DeadlineNotificationRequestedEvent.RouteInfo::getEstimatedDurationMin)
                .sum();

        double totalDistance = event.getRouteInfo().stream()
                .mapToDouble(DeadlineNotificationRequestedEvent.RouteInfo::getEstimatedDistanceKm)
                .sum();

        String requestMessage = event.getRequestMessage() == null || event.getRequestMessage().isBlank()
                ? "없음"
                : event.getRequestMessage();

        return """
                당신은 물류 배송 발송 시한 계산 AI입니다.

                아래 배송 정보를 기반으로 최종 발송 시한을 계산하세요.
                
                반드시 JSON 형식으로만 응답하세요.
                JSON 외부에 어떤 문장도 붙이지 마세요.
                응답 필드는 finalDepartureDeadline, message 두 개만 포함하세요.
                finalDepartureDeadline은 ISO-8601 형식으로 작성하세요. 예: 2025-12-10T09:00:00
                message는 Slack에 그대로 전송할 문자열입니다.
                message의 각 항목은 반드시 줄바꿈으로 구분하세요.
                message 값 내부의 줄바꿈은 \\\\n으로 표현하세요.
                추가 설명, 마크다운, 코드블록은 절대 포함하지 마세요.

                최종 발송 시한은 요청 도착 시각, 총 예상 이동 시간, 근무 시간을 고려해 계산하세요.

                [배송 정보]
                주문 ID: %s
                주문자 정보: %s / %s
                주문 시간: %s
                상품 정보: %s
                요청 사항: %s
                요청 도착 시각: %s
                발송지: %s
                도착지: %s

                배송 경로:
                %s

                배송담당자: %s / %s
                총 이동 거리: %.1fkm
                총 예상 이동 시간: %d분
                근무 시간: %s ~ %s

                [출력 형식]
                {
                  "finalDepartureDeadline": "YYYY-MM-DDTHH:mm:ss",
                  "message": "주문 ID : %s\\n주문자 정보 : %s / %s\\n주문 시간 : %s\\n상품 정보 : %s\\n요청 사항 : %s\\n발송지 : %s\\n도착지 : %s\\n배송담당자 : %s / %s\\n\\n위 내용을 기반으로 도출된 최종 발송 시한은 YYYY-MM-DD HH:mm 입니다."
                }
                """.formatted(
                event.getOrderId(),
                event.getOrdererName(),
                event.getOrdererEmail(),
                event.getOrderedAt().format(DATE_TIME_FORMATTER),
                products,
                requestMessage,
                event.getRequestedArrivalAt().format(DATE_TIME_FORMATTER),
                event.getDepartureHubName(),
                event.getDestinationAddress(),

                routeInfo,

                event.getDeliveryManagerName(),
                event.getDeliveryManagerEmail(),
                totalDistance,
                totalDuration,
                event.getWorkStartTime(),
                event.getWorkEndTime(),

                event.getOrderId(),
                event.getOrdererName(),
                event.getOrdererEmail(),
                event.getOrderedAt().format(DATE_TIME_FORMATTER),
                products,
                requestMessage,
                event.getDepartureHubName(),
                event.getDestinationAddress(),
                event.getDeliveryManagerName(),
                event.getDeliveryManagerEmail()
        );
    }
}