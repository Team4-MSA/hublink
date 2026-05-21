package com.msa.delivery_service.domain.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.delivery_service.domain.enums.DeliveryLocationType;
import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import com.msa.delivery_service.domain.enums.DeliveryRouteType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "p_delivery_route_histories", schema = "delivery_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeliveryRouteHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_route_history_id", nullable = false)
    private UUID deliveryRouteHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(name = "delivery_manager_id", nullable = false)
    private UUID deliveryManagerId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "route_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryRouteType routeType;

    @Column(name = "departure_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryLocationType departureType;

    @Column(name = "departure_id", nullable = false)
    private UUID departureId;

    @Column(name = "arrival_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryLocationType arrivalType;

    @Column(name = "arrival_id", nullable = false)
    private UUID arrivalId;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryRouteStatus status;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @Column(name = "estimated_distance_km", precision = 10, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(name = "estimated_duration_min")
    private Integer estimatedDurationMin;

    @Column(name = "actual_distance_km", precision = 10, scale = 2)
    private BigDecimal actualDistanceKm;

    @Column(name = "actual_duration_min")
    private Integer actualDurationMin;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static DeliveryRouteHistory create(
            Delivery delivery,
            Integer sequence,
            DeliveryRouteType routeType,
            DeliveryLocationType departureType,
            UUID departureId,
            DeliveryLocationType arrivalType,
            UUID arrivalId
    ) {
        return DeliveryRouteHistory.builder()
                .delivery(delivery)
                .sequence(sequence)
                .routeType(routeType)
                .departureType(departureType)
                .departureId(departureId)
                .arrivalType(arrivalType)
                .arrivalId(arrivalId)
                .status(DeliveryRouteStatus.PENDING)
                .build();
    }

    public void updateStatus(DeliveryRouteStatus status) {
        if (!this.status.canChangeTo(status)) {
            throw new IllegalStateException();
        }
        this.status = status;
    }

    public void updateStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void updateEstimatedRouteInfo(BigDecimal estimatedDistanceKm, Integer estimatedDurationMin) {
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public void complete(BigDecimal actualDistanceKm, Integer actualDurationMin) {
        this.status = DeliveryRouteStatus.COMPLETED;
        this.actualDistanceKm = actualDistanceKm;
        this.actualDurationMin = actualDurationMin;
        this.processedAt = LocalDateTime.now();
    }
}