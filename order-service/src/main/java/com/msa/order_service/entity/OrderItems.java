package com.msa.order_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.order_service.dto.res.ProductNPAResDto;
import com.msa.order_service.type.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "p_order_items")
@EntityListeners(AuditingEntityListener.class)
public class OrderItems extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    private UUID productId;

    private String productName;

    private UUID supplierCompanyId;

    private String supplierCompanyName;

    private UUID hubId;

    private Integer quantity;

    //상품 단가
    private Integer unitPrice;

    //수량*단가
    private Integer totalPrice;

    private Status status;

    public static OrderItems createOrderItem(
            Integer quantity,
            UUID supplierCompanyId,
            String supplierCompanyName,
            ProductNPAResDto productDto
    ) {
        OrderItems orderItem = new OrderItems();
        orderItem.productId = productDto.productId();
        orderItem.quantity = quantity;
        orderItem.supplierCompanyId = supplierCompanyId;
        orderItem.supplierCompanyName = supplierCompanyName;

        if (productDto != null && productDto.isSuccess()) {
            orderItem.productName = productDto.name();
            orderItem.hubId = productDto.hubId();
            orderItem.unitPrice = productDto.price();
            orderItem.totalPrice = productDto.price() * quantity;
            orderItem.status = Status.COMPLETED;
        } else {
            orderItem.productName = productDto != null ? productDto.name() : "존재하지 않는 상품";
            orderItem.hubId = productDto != null ? productDto.hubId() : null;
            orderItem.unitPrice = productDto != null ? productDto.price() : 0;
            orderItem.totalPrice = productDto != null ? productDto.price() * quantity : 0;
            orderItem.status = Status.FAILED;
        }

        return orderItem;
    }

    public void cancel() {

        if (this.status == Status.COMPLETED) {
            this.status = Status.CANCELED;
        }
    }

    public static OrderItems createFailedOrderItem(UUID productId, int quantity, UUID supplierCompanyId, String supplierCompanyName) {
        return OrderItems.builder()
                .productId(productId)
                .quantity(quantity)
                .supplierCompanyId(supplierCompanyId)
                .supplierCompanyName(supplierCompanyName)
                .productName("재고 부족 또는 존재하지 않는 상품") // 임의의 가이드 문자열 혹은 null
                .hubId(null)            // 실패했으므로 할당된 허브가 없음
                .unitPrice(0)           // 계산에서 제외되도록 0원 처리
                .totalPrice(0)
                .status(Status.FAILED)
                .build();
    }
}
