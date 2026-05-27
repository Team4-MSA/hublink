package com.msa.company_service.repository;

import com.msa.company_service.config.QueryDslConfig;
import com.msa.company_service.dto.CompanyRequest;
import com.msa.company_service.dto.CoordinateDto;
import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@Import(QueryDslConfig.class)
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    private final UUID HUB_ID = UUID.randomUUID();
    private final String NAME = "테스트 업체";
    private final CompanyType TYPE = CompanyType.SUPPLIER;
    private final String ADDRESS = "서울시 강남구 테헤란로";
    private final BigDecimal LAT = new BigDecimal("37.5");
    private final BigDecimal LON = new BigDecimal("127.0");

    @BeforeEach
    void setUp() {
        companyRepository.deleteAllInBatch(); // 초기화
    }

    @Test
    @DisplayName("성공: 허브ID, 이름, 타입, 주소가 모두 일치하면 true를 반환한다")
    void existsByHubIdAndNameAndTypeAndAddress_True() {
        // given
        CompanyRequest request = new CompanyRequest(HUB_ID, NAME, TYPE, ADDRESS, LAT, LON);
        CoordinateDto coordinate = new CoordinateDto(LAT, LON);

        CompanyEntity company = CompanyEntity.create(request, coordinate);
        companyRepository.save(company);

        // when
        boolean isExist = companyRepository.existsByHubIdAndNameAndTypeAndAddress(
                HUB_ID, NAME, TYPE, ADDRESS
        );

        // then
        assertThat(isExist).isTrue();
    }

    @Test
    @DisplayName("성공: 4개의 조건 중 하나라도 다르면 false를 반환한다")
    void existsByHubIdAndNameAndTypeAndAddress_False() {
        // given
        CompanyRequest request = new CompanyRequest(HUB_ID, NAME, TYPE, ADDRESS, LAT, LON);
        CoordinateDto coordinate = new CoordinateDto(LAT, LON);

        CompanyEntity company = CompanyEntity.create(request, coordinate);
        companyRepository.save(company);

        // when & then

        // 다른 ID
        boolean wrongHubId = companyRepository.existsByHubIdAndNameAndTypeAndAddress(
                UUID.randomUUID(), NAME, TYPE, ADDRESS
        );
        assertThat(wrongHubId).isFalse();

        // 다른 이름
        boolean wrongName = companyRepository.existsByHubIdAndNameAndTypeAndAddress(
                HUB_ID, "다른 이름", TYPE, ADDRESS
        );
        assertThat(wrongName).isFalse();

        // 다른 타입
        boolean wrongType = companyRepository.existsByHubIdAndNameAndTypeAndAddress(
                HUB_ID, NAME, CompanyType.RECEIVER, ADDRESS
        );
        assertThat(wrongType).isFalse();

        // 다른 주소
        boolean wrongAddress = companyRepository.existsByHubIdAndNameAndTypeAndAddress(
                HUB_ID, NAME, TYPE, "다른 주소"
        );
        assertThat(wrongAddress).isFalse();
    }
}