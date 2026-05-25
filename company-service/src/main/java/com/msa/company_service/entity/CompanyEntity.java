package com.msa.company_service.entity;

import com.msa.company_service.dto.CompanyRequest;
import com.msa.company_service.dto.CompanyUpdateRequest;
import com.msa.company_service.dto.CoordinateDto;
import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_companies")
@SQLRestriction("deleted_at IS NULL")
public class CompanyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id")
    private UUID companyId;

    @Column(nullable = false)
    private UUID hubId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private CompanyType type;

    @Column(nullable = false)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    public static CompanyEntity create(CompanyRequest request, CoordinateDto coordinateDto) {
        return CompanyEntity.builder()
                .hubId(request.hubId())
                .name(request.name())
                .type(request.type())
                .address(request.address())
                .latitude(coordinateDto.latitude())
                .longitude(coordinateDto.longitude())
                .build();
    }

    public void update(CompanyUpdateRequest request, CoordinateDto coordinateDto) {
        if (request.hubId() != null) {
            this.hubId = request.hubId();
        }
        if (request.name() != null) {
            this.name = request.name();
        }
        if (request.type() != null) {
            this.type = request.type();
        }
        if (request.address() != null) {
            this.address = request.address();
        }
        if (coordinateDto != null) {
            this.latitude = coordinateDto.latitude();
            this.longitude = coordinateDto.longitude();
        }
    }
}
