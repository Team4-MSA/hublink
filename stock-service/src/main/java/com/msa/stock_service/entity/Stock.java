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
    @Version
    private Integer version;

    //재고 생성
    public static Stock create(StockRequestDto dto){
        return com.msa.stock_service.entity.Stock.builder()
            .productId(dto.getProductId())
            .hubId(dto.getHubId())
            .quantity(dto.getQuantity())
            .build();
    }

    // 재고 감소.
    public void decreaseStock(Integer quantity){
        this.quantity -= quantity;
        this.reservedQuantity += quantity;
    }

    //재고 복원
    public void restore(Integer orderQuantity){
        this.quantity += orderQuantity;
        this.reservedQuantity -= orderQuantity;
    }
    public Stock modifyQuantity(Integer newQuantity){
        if(newQuantity == null || newQuantity < 0){
            throw new IllegalArgumentException("재고 수량은 0 이상이어야함.");
        }
        if(newQuantity < this.reservedQuantity) {
            throw new IllegalArgumentException("예약 재고 보다는 많아야 함");
        }
        this.quantity = newQuantity;
        return this;
    }
}
