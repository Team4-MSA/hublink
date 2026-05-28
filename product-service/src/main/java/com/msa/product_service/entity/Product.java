package com.msa.product_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.product_service.dto.ProductRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "p_product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID id;

    @Column(name="company_id",nullable = false)
    private UUID companyId;

    @Column(name = "hub_id",nullable = false)
    private UUID hubId;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Version
    private Integer version;

    public static Product create (UUID companyId, UUID hubId, String name, Integer price, String description) {
        return Product.builder()
            .companyId(companyId)
            .hubId(hubId)
            .name(name)
            .description(description)
            .price(price)
            .build();
    }

    //상품 수정
    public Product modifyProduct(UUID companyId, UUID hubId, String name, Integer price, String description) {
        this.companyId = companyId;
        this.hubId = hubId;
        this.price = price;
        this.name = name;
        this.description = description;
        return this;
    }
}
