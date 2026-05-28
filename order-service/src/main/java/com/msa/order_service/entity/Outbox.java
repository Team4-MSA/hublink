package com.msa.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_outbox")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    private String aggregateType; // 어떤 도메인인지 (예: "DELIVERY", "USER")
    private String aggregateId;   // 해당 도메인의 PK (ID값)
    private String topic;         // 발행할 카프카 토픽명

    @Column(columnDefinition = "TEXT")
    private String payload;       // JSON 문자열 객체 데이터 본체

    private boolean processed;    // 카프카 전송 완료 여부 (true/false)
    private LocalDateTime createdAt;

    //발행 완료 처리
    public void markProcessed() {
        this.processed = true;
    }

}
