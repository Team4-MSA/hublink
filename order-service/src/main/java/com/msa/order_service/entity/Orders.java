package com.msa.order_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.core_common.error.exception.CustomException;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.error.OrderErrorCode;
import com.msa.order_service.type.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Entity
@Table(name = "p_orders")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID supplierCompanyId;

    @Column(nullable = false)
    private UUID receiverCompanyId;

    private UUID orderedByUserId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(columnDefinition = "text")
    private String requestMemo;

    @Column(nullable = false)
    private LocalDateTime requestedDeliveryDeadline;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItems> orderItems = new ArrayList<>();

    public static Orders createInitOrder(OrderMakeReqDto orderMakeReqDto, UUID userId) {
        Orders order = new Orders();
        order.supplierCompanyId = orderMakeReqDto.getSupplierCompanyId();
        order.receiverCompanyId = orderMakeReqDto.getReceiverCompanyId();
        order.requestMemo = orderMakeReqDto.getRequestMemo();
        order.requestedDeliveryDeadline = orderMakeReqDto.getRequestedDeliveryDeadline();
        order.orderedByUserId = userId;
        order.status = Status.CREATED;
        return order;
    }

    public void addOrderItem(OrderItems orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this); // 자식에게 부모 연결
    }

    public void updateTotalPrice() {
        this.totalPrice = this.orderItems.stream()
                .mapToInt(OrderItems::getTotalPrice)
                .sum();
    }

    public void cancel() {

        if (this.status == Status.CANCELED) {
            throw new CustomException(OrderErrorCode.ALREADY_CANCELED);
        }

        // 1. 부모 주문 상태를 취소로 변경
        this.status = Status.CANCELED;

        // 2. 양방향으로 연결된 자식 아이템들도 함께 취소 처리
        if (this.orderItems != null) {
            for (OrderItems item : this.orderItems) {
                item.cancel();
            }
        }
    }
}
