package com.msa.stock_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.stock_service.dto.StockRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "p_stock")
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(nullable = false)
    private Integer quantity;

    @Builder.Default
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    //재고 감소 기능은 빈번하게 발생되는 동시성 문제이기 때문에
    // 비관적 락으로 해결할 것이므로 Version 컬럼을 제외한다.
//    @Version
//    private Integer version;

    public static Stock create(StockRequestDto dto){
        return Stock.builder()
            .productId(dto.getProductId())
            .hubId(dto.getHubId())
            .quantity(dto.getQuantity())
            .build();
    }

    // 재고 감소.
    public void decreaStock(Integer quantity){
        this.quantity = this.quantity - quantity;
        this.reservedQuantity = quantity;
    }

}
