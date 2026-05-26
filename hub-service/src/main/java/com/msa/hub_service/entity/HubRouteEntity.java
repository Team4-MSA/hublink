package com.msa.hub_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.core_common.error.exception.CustomException;
import com.msa.hub_service.dto.RouteCalculationResult;
import com.msa.hub_service.global.HubErrorCode;
import com.msa.hub_service.global.Util;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_hub_routes")
@SQLRestriction("deleted_at IS NULL")
public class HubRouteEntity extends BaseEntity {

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

    public static HubRouteEntity create(HubEntity departureHub, HubEntity arrivalHub) {
        validateHubs(departureHub, arrivalHub);

        RouteCalculationResult routeInfo = Util.RouteCalculator.calculate(departureHub.getLatitude(), departureHub.getLongitude(), arrivalHub.getLatitude(), arrivalHub.getLongitude());

        return HubRouteEntity.builder().departureHub(departureHub).arrivalHub(arrivalHub).estimatedDistanceKm(routeInfo.distanceKm()).estimatedDurationMin(routeInfo.durationMin()).routeType(routeInfo.routeType()).build();
    }

    public void update(BigDecimal targetKm, Integer targetMin, RouteType targetType) {
        this.estimatedDistanceKm = targetKm;
        this.estimatedDurationMin = targetMin;
        this.routeType = targetType;
    }

    public void recalculateRouteInfo() {
        RouteCalculationResult routeInfo = Util.RouteCalculator.calculate(departureHub.getLatitude(), departureHub.getLongitude(), arrivalHub.getLatitude(), arrivalHub.getLongitude());

        this.estimatedDistanceKm = routeInfo.distanceKm();
        this.estimatedDurationMin = routeInfo.durationMin();
        this.routeType = routeInfo.routeType();

    }

    private static void validateHubs(HubEntity dep, HubEntity arr) {
        if (dep == null) throw new CustomException(HubErrorCode.DEPARTURE_HUB_REQUIRED);
        if (arr == null) throw new CustomException(HubErrorCode.ARRIVAL_HUB_REQUIRED);
        if (dep.getHubId().equals(arr.getHubId())) throw new CustomException(HubErrorCode.SAME_HUB_NOT_ALLOWED);

        if (dep.getLatitude() == null || dep.getLongitude() == null || arr.getLatitude() == null || arr.getLongitude() == null) {
            throw new CustomException(HubErrorCode.NULL_COORDINATES);
        }
    }

}
