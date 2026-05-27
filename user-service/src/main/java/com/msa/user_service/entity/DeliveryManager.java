package com.msa.user_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "p_delivery_managers", schema = "user_service")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class DeliveryManager extends BaseEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "hub_id", columnDefinition = "uuid")
    private UUID hubId;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DeliveryManagerType type;

    @Column(name = "delivery_sequence", nullable = false)
    private Integer deliverySequence;

    @Column(name = "slack_id", length = 100)
    private String slackId;

    public void update(DeliveryManagerType type, String slackId) {
        if (type != null) this.type = type;
        if (slackId != null) this.slackId = slackId;
    }

    public void changeHub(UUID hubId, int deliverySequence) {
        this.hubId = hubId;
        this.deliverySequence = deliverySequence;
    }

    public void updateSlackId(String slackId) {
        this.slackId = slackId;
    }
}
