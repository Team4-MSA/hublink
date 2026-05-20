package com.msa.hub_service.entity;

import com.msa.hub_service.global.Util;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_hub_routes")
public class HubRouteEntity {

    // 물류 화물차량의 평균 주행 속도 (60 km/h)
    private static final double AVERAGE_TRUCK_SPEED_KMH = 60.0;

    // 도로 우회 계수 (직선 거리 대비 실제 주행 거리의 비율, 약 30% 증가)
    private static final double ROAD_CURVATURE_WEIGHT = 1.3;

    // 허브 간(H2H) 경로로 판별하기 위한 기준 거리 (km)
    private static final double H2H_DISTANCE_THRESHOLD_KM = 200.0;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_route_id")
    private UUID hubRouteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_hub_id", nullable = false)
    private HubEntity departureHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_hub_id", nullable = false)
    private HubEntity arrivalHub;

    @Column(name = "estimated_distance_km", precision = 10, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(name = "estimated_duration_min")
    private Integer estimatedDurationMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", length = 50)
    private RouteType routeType;

    public static HubRouteEntity create(
            HubEntity departureHub,
            HubEntity arrivalHub
    ) {
        Assert.notNull(departureHub, "출발 허브 ID는 필수입니다.");
        Assert.notNull(arrivalHub, "도착 허브 ID는 필수입니다.");

        if (departureHub.getHubId().equals(arrivalHub.getHubId())) {
            throw new IllegalArgumentException("출발 허브와 도착 허브는 같을 수 없습니다.");
        }

        // 위경도를 이용한 직선 거리 계산 (km)
        double straightDistance = Util.DistanceCalculator.getDistance(
                departureHub.getLatitude(), departureHub.getLongitude(),
                arrivalHub.getLatitude(), arrivalHub.getLongitude()
        );

        // 우회 계수를 적용하여 실제 주행 예상 거리 산출
        double actualDrivingDistance = straightDistance * ROAD_CURVATURE_WEIGHT;

        BigDecimal estimatedDistanceKm = BigDecimal.valueOf(actualDrivingDistance)
                .setScale(2, RoundingMode.HALF_UP);

        // 보정된 주행 거리를 바탕으로 예상 소요 시간 계산 (분 단위)
        double durationMinutes = (actualDrivingDistance / AVERAGE_TRUCK_SPEED_KMH) * 60.0;
        int estimatedDurationMin = (int) Math.round(durationMinutes);

        RouteType routeType = determineRouteType(actualDrivingDistance);

        return HubRouteEntity.builder()
                .departureHub(departureHub)
                .arrivalHub(arrivalHub)
                .estimatedDistanceKm(estimatedDistanceKm)
                .estimatedDurationMin(estimatedDurationMin)
                .routeType(routeType)
                .build();
    }

    private static RouteType determineRouteType(double distanceKm) {
        if (distanceKm > H2H_DISTANCE_THRESHOLD_KM) {
            return RouteType.H2H;
        }
        return RouteType.P2P;
    }
}
