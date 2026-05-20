package com.msa.hub_service.entity;

import com.msa.core_common.JpaAuditing.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "p_hubs")
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

    // 정상 등록
    public static HubEntity create(String name, String address, BigDecimal latitude, BigDecimal longitude) {
        Assert.hasText(name, "허브 이름은 필수입니다.");
        Assert.hasText(address, "허브 주소는 필수입니다.");
        Assert.notNull(latitude, "위도는 필수입니다.");
        Assert.notNull(longitude, "경도는 필수입니다.");

        validateCoordinates(latitude, longitude);

        return HubEntity.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    // 이름과 주소만 일단 등록
    public static HubEntity createPendingCoordinates(String name, String address) {
        Assert.hasText(name, "허브 이름은 필수입니다.");
        Assert.hasText(address, "허브 주소는 필수입니다.");

        return HubEntity.builder()
                .name(name)
                .address(address)
                // latitude, longitude는 자동으로 null이 됨
                .build();
    }

    //누락 좌표 업데이트
    public void updateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        Assert.notNull(latitude, "위도는 필수입니다.");
        Assert.notNull(longitude, "경도는 필수입니다.");

        validateCoordinates(latitude, longitude);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    private static void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("위도는 -90에서 90 사이여야 합니다. 입력값: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("경도는 -180에서 180 사이여야 합니다. 입력값: " + longitude);
        }
    }

}
