package com.msa.hub_service.repository;

import com.msa.hub_service.config.QueryDslConfig;
import com.msa.hub_service.entity.HubEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@Import(QueryDslConfig.class)
class HubRepositoryTest {

    @Autowired
    private HubRepository hubRepository;

    @Test
    @DisplayName("성공: 위도(Latitude)가 null인 허브 목록만 정확히 조회한다")
    void findByLatitudeIsNull_Success() {
        // given
        HubEntity normalHub = HubEntity.create("정상 허브", "서울", new BigDecimal("37.5"), new BigDecimal("126.9"));
        HubEntity noLatHub = HubEntity.create("좌표 없는 허브", "부산", null, new BigDecimal("129.0"));

        hubRepository.save(normalHub);
        hubRepository.save(noLatHub);

        // when
        List<HubEntity> result = hubRepository.findByLatitudeIsNull();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("좌표 없는 허브");
        assertThat(result.get(0).getLatitude()).isNull();
    }

    @Test
    @DisplayName("성공: 이름에 특정 키워드가 포함된 허브를 페이징 처리하여 조회한다")
    void findByNameContaining_Success() {
        // given
        HubEntity hub1 = HubEntity.create("서울 중앙 허브", "주소1", new BigDecimal("37"), new BigDecimal("126"));
        HubEntity hub2 = HubEntity.create("서울 동부 허브", "주소2", new BigDecimal("37"), new BigDecimal("126"));
        HubEntity hub3 = HubEntity.create("부산 허브", "주소3", new BigDecimal("35"), new BigDecimal("129"));

        hubRepository.saveAll(List.of(hub1, hub2, hub3));
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<HubEntity> result = hubRepository.findByNameContaining("서울", pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(HubEntity::getName)
                .containsExactlyInAnyOrder("서울 중앙 허브", "서울 동부 허브");
    }

    @Test
    @DisplayName("성공: 이미 등록된 이름이 있는지 존재 여부를 확인한다")
    void existsByName_Success() {
        // given
        String existingName = "인천 허브";
        HubEntity hub = HubEntity.create(existingName, "인천", new BigDecimal("37"), new BigDecimal("126"));
        hubRepository.save(hub);

        // when & then
        assertThat(hubRepository.existsByName(existingName)).isTrue();
        assertThat(hubRepository.existsByName("없는 허브 이름")).isFalse();
    }

    @Test
    @DisplayName("성공: 이미 등록된 주소가 있는지 존재 여부를 확인한다")
    void existsByAddress_Success() {
        // given
        String existingAddress = "경기도 수원시";
        HubEntity hub = HubEntity.create("경기 허브", existingAddress, new BigDecimal("37"), new BigDecimal("126"));
        hubRepository.save(hub);

        // when & then
        assertThat(hubRepository.existsByAddress(existingAddress)).isTrue();
        assertThat(hubRepository.existsByAddress("새로운 주소")).isFalse();
    }

    @Test
    @DisplayName("성공: 특정 허브 ID를 제외한 나머지 허브 목록을 조회한다")
    void findByHubIdNot_Success() {
        // given
        HubEntity hubA = HubEntity.create("허브A", "주소A", new BigDecimal("37"), new BigDecimal("126"));
        HubEntity hubB = HubEntity.create("허브B", "주소B", new BigDecimal("37"), new BigDecimal("126"));
        HubEntity hubC = HubEntity.create("허브C", "주소C", new BigDecimal("37"), new BigDecimal("126"));

        hubRepository.saveAll(List.of(hubA, hubB, hubC));

        var idA = hubA.getHubId();

        // when
        List<HubEntity> result = hubRepository.findByHubIdNot(idA);

        // then
        assertThat(result).hasSize(2); // A를 제외했으므로 2개여야 함
        assertThat(result)
                .extracting(HubEntity::getName)
                .containsExactlyInAnyOrder("허브B", "허브C")
                .doesNotContain("허브A");
    }
}