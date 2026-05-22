package com.msa.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_user_approval_histories", schema = "user_service")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class UserApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "approval_history_id", columnDefinition = "uuid")
    private UUID approvalHistoryId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "previous_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserStatus previousStatus;

    @Column(name = "new_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserStatus newStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "processed_by")
    private UUID processedBy;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onPersist() {
        this.processedAt = LocalDateTime.now();
    }
}
