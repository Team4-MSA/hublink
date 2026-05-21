package com.msa.hub_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import com.msa.core_common.error.exception.CustomException;
import com.msa.hub_service.global.HubErrorCode;
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
@Table(name = "p_hubs")
@SQLRestriction("deleted_at IS NULL")
public class HubEntity extends BaseEntity {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90.0");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90.0");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180.0");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180.0");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_id")
    private UUID hubId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // 등록
    public static HubEntity create(String name, String address, BigDecimal latitude, BigDecimal longitude) {
        validateNotBlank(name, HubErrorCode.HUB_NAME_REQUIRED);
        validateNotBlank(address, HubErrorCode.HUB_ADDRESS_REQUIRED);

        if (latitude != null && longitude != null) {
            validateCoordinates(latitude, longitude);
        }

        return HubEntity.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    // 누락 좌표 업데이트
    public void updateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new CustomException(HubErrorCode.NULL_COORDINATES);
        }

        validateCoordinates(latitude, longitude);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 주소 변경
    public void updateHub(String name, String address, BigDecimal latitude, BigDecimal longitude) {

        validateNotBlank(name, HubErrorCode.HUB_NAME_REQUIRED);
        validateNotBlank(address, HubErrorCode.HUB_ADDRESS_REQUIRED);

        if (latitude != null && longitude != null) {
            validateCoordinates(latitude, longitude);
        }

        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 위도 경도 유효성 검사
    private static void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0 ||
                longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new CustomException(HubErrorCode.INVALID_COORDINATES);
        }
    }

    // 문자열 유효성 검사
    private static void validateNotBlank(String value, HubErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new CustomException(errorCode);
        }
    }

}
