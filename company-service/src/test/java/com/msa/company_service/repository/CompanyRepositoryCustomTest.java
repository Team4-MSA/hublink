package com.msa.company_service.repository;

import com.msa.company_service.config.QueryDslConfig;
import com.msa.company_service.dto.CompanyRequest;
import com.msa.company_service.dto.CoordinateDto;
import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyInfo;
import com.msa.company_service.entity.CompanyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

@DataJpaTest
@Import(QueryDslConfig.class)
class CompanyRepositoryCustomTest {

    @Autowired
    private CompanyRepository companyRepository;

    private final UUID HUB_1 = UUID.randomUUID();
    private final UUID HUB_2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        companyRepository.deleteAllInBatch();
        // 테스트 데이터
        saveCompany(HUB_1, "A-테스트 업체", CompanyType.SUPPLIER, "서울시 강남구");
        saveCompany(HUB_1, "B-테스트 업체", CompanyType.RECEIVER, "서울시 서초구");
        saveCompany(HUB_2, "C-테스트 업체", CompanyType.SUPPLIER, "부산광역시 해운대구");
    }

    private void saveCompany(UUID hubId, String name, CompanyType type, String address) {
        CompanyRequest request = new CompanyRequest(hubId, name, type, address, new BigDecimal("37.0"), new BigDecimal("127.0"));
        CoordinateDto coordinate = new CoordinateDto(new BigDecimal("37.0"), new BigDecimal("127.0"));

        CompanyInfo info = new CompanyInfo(
                request.hubId(),
                request.name(),
                request.type(),
                request.address(),
                coordinate.latitude(),
                coordinate.longitude()
        );

        companyRepository.save(CompanyEntity.create(info));
    }

    @Test
    @DisplayName("성공: 조건 없음 - 전체 반환")
    void searchCompanies_NoConditions_ReturnsAll() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<CompanyEntity> result = companyRepository.searchCompanies(null, null, null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("성공: hubId로 검색")
    void searchCompanies_ByHubId() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<CompanyEntity> result = companyRepository.searchCompanies(HUB_1, null, null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(CompanyEntity::getName)
                .containsExactlyInAnyOrder("A-테스트 업체", "B-테스트 업체");
    }

    @Test
    @DisplayName("성공: 검색 필터링")
    void searchCompanies_ByNameAndType() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<CompanyEntity> result = companyRepository.searchCompanies(null, "테스트", CompanyType.SUPPLIER, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(CompanyEntity::getName)
                .containsExactlyInAnyOrder("A-테스트 업체", "C-테스트 업체");
    }

    @Test
    @DisplayName("성공: 주소로 검색")
    void searchCompanies_ByAddress() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<CompanyEntity> result = companyRepository.searchCompanies(null, null, null, "서울", pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(CompanyEntity::getName)
                .containsExactlyInAnyOrder("A-테스트 업체", "B-테스트 업체");
    }

    @Test
    @DisplayName("성공: 정렬 확인")
    void searchCompanies_WithSorting() {
        // given
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));

        // when
        Page<CompanyEntity> result = companyRepository.searchCompanies(null, null, null, null, pageable);

        // then
        assertThat(result.getContent().get(0).getName()).isEqualTo("C-테스트 업체");
        assertThat(result.getContent().get(1).getName()).isEqualTo("B-테스트 업체");
        assertThat(result.getContent().get(2).getName()).isEqualTo("A-테스트 업체");
    }
}