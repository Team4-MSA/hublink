package com.msa.delivery_service.domain.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "p_deliveries", schema = "delivery_service")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "departure_hub_id", nullable = false)
    private UUID departureHubId;

    @Column(name = "destination_hub_id", nullable = false)
    private UUID destinationHubId;

    @Column(name = "receiver_company_id", nullable = false)
    private UUID receiverCompanyId;

    @Column(name = "company_delivery_manager_id", nullable = false)
    private UUID companyDeliveryManagerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "receiver_name", length = 100, nullable = false)
    private String receiverName;

    @Column(name = "hub_manager_slack_id", length = 100,  nullable = false)
    private String hubManagerSlackId;

    @Column(name = "estimated_arrival_at")
    private LocalDateTime estimatedArrivalAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "final_departure_deadline")
    private LocalDateTime finalDepartureDeadline;

    public static Delivery create(
            UUID orderId,
            UUID departureHubId,
            UUID destinationHubId,
            UUID receiverCompanyId,
            UUID companyDeliveryManagerId,
            String deliveryAddress,
            String receiverName,
            String hubManagerSlackId
    ) {
        return Delivery.builder()
                .orderId(orderId)
                .departureHubId(departureHubId)
                .destinationHubId(destinationHubId)
                .receiverCompanyId(receiverCompanyId)
                .companyDeliveryManagerId(companyDeliveryManagerId)
                .status(DeliveryStatus.PENDING)
                .deliveryAddress(deliveryAddress)
                .receiverName(receiverName)
                .hubManagerSlackId(hubManagerSlackId)
                .build();
    }

    public void updateStatus(DeliveryStatus status) {
        if (!this.status.canChangeTo(status)) {
            throw new CustomException(DeliveryErrorCode.INVALID_DELIVERY_STATUS_TRANSITION);
        }
        this.status = status;
    }

    public void updateEstimatedArrival(LocalDateTime estimatedArrivalAt) {
        this.estimatedArrivalAt = estimatedArrivalAt;
    }

    public void updateFinalDepartureDeadline(LocalDateTime finalDepartureDeadline) {
        this.finalDepartureDeadline = finalDepartureDeadline;
    }

    public void complete() {
        updateStatus(DeliveryStatus.DELIVERED);
        this.deliveredAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == DeliveryStatus.CANCELLED) return;
        updateStatus(DeliveryStatus.CANCELLED);
    }
}
