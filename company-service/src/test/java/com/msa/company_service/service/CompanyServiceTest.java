package com.msa.company_service.service;

import com.msa.company_service.client.HubClient;
import com.msa.company_service.dto.*;
import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyInfo;
import com.msa.company_service.entity.CompanyType;
import com.msa.company_service.global.CompanyErrorCode;
import com.msa.company_service.repository.CompanyRepository;
import com.msa.core_common.auth.UserRole;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private HubClient hubClient;
    @Mock
    private AuditorAware<String> auditorAware;

    @InjectMocks
    private CompanyService companyService;

    private final UUID HUB_ID = UUID.randomUUID();
    private final UUID COMPANY_ID = UUID.randomUUID();
    private final BigDecimal LAT = new BigDecimal("37.0");
    private final BigDecimal LON = new BigDecimal("127.0");

    private CompanyEntity createCompanyEntity(UUID id, UUID hubId, String name, String address) {
        CompanyRequest req = new CompanyRequest(hubId, name, CompanyType.SUPPLIER, address, LAT, LON);

        CompanyInfo info = new CompanyInfo(
                req.hubId(),
                req.name(),
                req.type(),
                req.address(),
                req.latitude(),
                req.longitude()
        );

        CompanyEntity entity = CompanyEntity.create(info);
        ReflectionTestUtils.setField(entity, "companyId", id);
        return entity;
    }

    @Nested
    @DisplayName("업체 생성 테스트")
    class CreateCompanyTest {

        @Test
        @DisplayName("성공: 모든 검증을 통과하고 업체를 생성한다 (좌표 직접 입력)")
        void createCompany_Success() {
            // given
            CompanyRequest request = new CompanyRequest(HUB_ID, "테스트업체", CompanyType.SUPPLIER, "주소", LAT, LON);

            given(companyRepository.existsByHubIdAndNameAndTypeAndAddress(any(), any(), any(), any())).willReturn(false);
            given(hubClient.getHubExist(HUB_ID)).willReturn(true);

            doAnswer(inv -> {
                CompanyEntity e = inv.getArgument(0);
                ReflectionTestUtils.setField(e, "companyId", COMPANY_ID);
                return e;
            }).when(companyRepository).save(any(CompanyEntity.class));

            // when
            CompanyResponse response = companyService.createCompany(request, UserRole.MASTER, null);

            // then
            assertThat(response.companyId()).isEqualTo(COMPANY_ID);
            assertThat(response.name()).isEqualTo("테스트업체");
            verify(hubClient, never()).getCoordinates(anyString()); // 직접 입력했으니 통신 안 함
        }

        @Test
        @DisplayName("실패: HUB_MANAGER 권한인데, 요청한 hubId가 자신의 관할이 아니면 FORBIDDEN 예외")
        void createCompany_Fail_Forbidden_HubManager() {
            // given
            UUID otherHubId = UUID.randomUUID();
            CompanyRequest request = new CompanyRequest(HUB_ID, "테스트업체", CompanyType.SUPPLIER, "주소", LAT, LON);

            // when & then
            assertThatThrownBy(() -> companyService.createCompany(request, UserRole.HUB_MANAGER, otherHubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CompanyErrorCode.FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("성공: 좌표 미입력 시 HubClient를 호출하여 좌표를 가져온 뒤 생성한다")
        void createCompany_Success_WithHubClient() {
            // given
            CompanyRequest request = new CompanyRequest(HUB_ID, "테스트업체", CompanyType.SUPPLIER, "주소", null, null);
            CoordinateDto apiCoord = new CoordinateDto(LAT, LON);

            given(companyRepository.existsByHubIdAndNameAndTypeAndAddress(any(), any(), any(), any())).willReturn(false);
            given(hubClient.getHubExist(HUB_ID)).willReturn(true);
            given(hubClient.getCoordinates("주소")).willReturn(apiCoord);

            // when
            CompanyResponse response = companyService.createCompany(request, UserRole.MASTER, null);

            // then
            assertThat(response.name()).isEqualTo("테스트업체");
            verify(hubClient, times(1)).getCoordinates("주소");
        }
    }

    @Nested
    @DisplayName("업체 수정 테스트")
    class UpdateCompanyTest {

        @Test
        @DisplayName("성공: 주소가 변경되고 좌표 미입력 시 HubClient를 통해 갱신한다")
        void updateCompany_AddressChanged_UseHubClient() {
            // given
            CompanyEntity company = createCompanyEntity(COMPANY_ID, HUB_ID, "기존업체", "기존주소");
            CompanyUpdateRequest request = new CompanyUpdateRequest(HUB_ID, "새업체", CompanyType.RECEIVER, "새주소", null, null);
            CoordinateDto apiCoord = new CoordinateDto(new BigDecimal("38.0"), new BigDecimal("128.0"));

            given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
            given(hubClient.getCoordinates("새주소")).willReturn(apiCoord);

            // when
            CompanyResponse response = companyService.updateCompany(COMPANY_ID, request, null, UserRole.MASTER, null);

            // then
            assertThat(response.address()).isEqualTo("새주소");
            verify(hubClient, times(1)).getCoordinates("새주소");
            assertThat(company.getLatitude()).isEqualTo(new BigDecimal("38.0"));
        }

        @Test
        @DisplayName("실패: COMPANY_MANAGER 권한인데, 관할 업체가 아니면 FORBIDDEN 예외")
        void updateCompany_Fail_Forbidden_CompanyManager() {
            // given
            CompanyEntity company = createCompanyEntity(COMPANY_ID, HUB_ID, "업체", "주소");
            CompanyUpdateRequest request = new CompanyUpdateRequest(HUB_ID, "새업체", CompanyType.RECEIVER, "새주소", LAT, LON);
            UUID otherCompanyId = UUID.randomUUID(); // 담당이 아닌 업체

            given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));

            // when & then
            assertThatThrownBy(() -> companyService.updateCompany(COMPANY_ID, request, otherCompanyId, UserRole.COMPANY_MANAGER, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CompanyErrorCode.FORBIDDEN.getMessage());
        }
    }

    @Nested
    @DisplayName("업체 삭제 테스트")
    class DeleteCompanyTest {

        @Test
        @DisplayName("성공: MASTER 권한으로 업체를 삭제(Soft Delete)한다")
        void deleteCompany_Success() {
            // given
            CompanyEntity company = createCompanyEntity(COMPANY_ID, HUB_ID, "업체", "주소");
            given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
            given(auditorAware.getCurrentAuditor()).willReturn(Optional.of("ADMIN_USER"));

            // when
            CompanyResponse response = companyService.deleteCompany(COMPANY_ID, UserRole.MASTER, null);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("단건/다건 조회 및 유틸 메서 테스트")
    class RetrieveCompanyTest {

        @Test
        @DisplayName("성공: 업체 단건 조회")
        void getCompany_Success() {
            CompanyEntity company = createCompanyEntity(COMPANY_ID, HUB_ID, "업체", "주소");
            given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));

            CompanyResponse response = companyService.getCompany(COMPANY_ID);
            assertThat(response.companyId()).isEqualTo(COMPANY_ID);
        }

        @Test
        @DisplayName("성공: 업체 검색 (페이징)")
        void getCompanies_Success() {
            CompanyEntity company = createCompanyEntity(COMPANY_ID, HUB_ID, "검색업체", "주소");
            PageRequest pageable = PageRequest.of(0, 10);
            Page<CompanyEntity> page = new PageImpl<>(List.of(company));

            given(companyRepository.searchCompanies(any(), any(), any(), any(), eq(pageable))).willReturn(page);

            PageRes<CompanyResponse> response = companyService.getCompanies(HUB_ID, "검색업체", null, null, pageable);

            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공: 업체 여러 개 이름 조회 (in 쿼리 테스트)")
        void getCompanyNames_Success() {
            CompanyEntity company1 = createCompanyEntity(UUID.randomUUID(), HUB_ID, "업체1", "주소1");
            CompanyEntity company2 = createCompanyEntity(UUID.randomUUID(), HUB_ID, "업체2", "주소2");
            List<UUID> ids = List.of(company1.getCompanyId(), company2.getCompanyId());

            given(companyRepository.findAllById(ids)).willReturn(List.of(company1, company2));

            List<CompanyNameResponse> responses = companyService.getCompanyNames(ids);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).name()).isEqualTo("업체1");
        }

        @Test
        @DisplayName("성공: 빈 리스트로 업체 이름 조회 시 빈 리스트 반환 (쿼리 안 날림)")
        void getCompanyNames_EmptyList() {
            List<CompanyNameResponse> responses = companyService.getCompanyNames(Collections.emptyList());

            assertThat(responses).isEmpty();
            verify(companyRepository, never()).findAllById(any());
        }
    }
}