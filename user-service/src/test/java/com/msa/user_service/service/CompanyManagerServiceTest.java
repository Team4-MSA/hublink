package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.CompanyClient;
import com.msa.user_service.dto.CompanyExistsResponse;
import com.msa.user_service.dto.CompanyManagerRequest;
import com.msa.user_service.dto.CompanyManagerResponse;
import com.msa.user_service.entity.CompanyManager;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.CompanyManagerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyManagerService 테스트")
class CompanyManagerServiceTest {

    @Mock private CompanyManagerRepository companyManagerRepository;
    @Mock private CompanyClient companyClient;

    @InjectMocks
    private CompanyManagerService companyManagerService;

    @Test
    @DisplayName("업체 존재 검증 실패 - 없는 업체")
    void validateCompanyExists_notFound() {
        // given
        given(companyClient.checkCompanyExists(TestFixtures.COMPANY_ID))
                .willReturn(companyExistsResponse(false));

        // then
        assertThatThrownBy(() -> companyManagerService.validateCompanyExists(TestFixtures.COMPANY_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.COMPANY_NOT_FOUND));
    }

    @Test
    @DisplayName("업체 담당자 등록 성공")
    void register_success() {
        // given
        CompanyManagerRequest request = companyManagerRequest(TestFixtures.USER_ID, TestFixtures.COMPANY_ID);
        CompanyManager saved = TestFixtures.companyManager();

        given(companyClient.checkCompanyExists(TestFixtures.COMPANY_ID)).willReturn(companyExistsResponse(true));
        given(companyManagerRepository.save(any(CompanyManager.class))).willReturn(saved);

        // when
        CompanyManagerResponse response = companyManagerService.register(request);

        // then
        assertThat(response.getCompanyManagerId()).isEqualTo(TestFixtures.COMPANY_MANAGER_ID);
        assertThat(response.getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(response.getCompanyId()).isEqualTo(TestFixtures.COMPANY_ID);
    }

    @Test
    @DisplayName("업체 담당자 등록 실패 - 업체 없음")
    void register_companyNotFound() {
        // given
        CompanyManagerRequest request = companyManagerRequest(TestFixtures.USER_ID, TestFixtures.COMPANY_ID);
        given(companyClient.checkCompanyExists(TestFixtures.COMPANY_ID)).willReturn(companyExistsResponse(false));

        // then
        assertThatThrownBy(() -> companyManagerService.register(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.COMPANY_NOT_FOUND));
    }

    @Test
    @DisplayName("업체 담당자 목록 조회 - 전체")
    void getList_noFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(companyManagerRepository.findAllByDeletedAtIsNull(pageable))
                .willReturn(new PageImpl<>(List.of(TestFixtures.companyManager())));

        // when
        PageRes<CompanyManagerResponse> result = companyManagerService.getList(null, pageable);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("업체 담당자 목록 조회 - 업체 ID 필터")
    void getList_withCompanyFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(companyManagerRepository.findAllByCompanyIdAndDeletedAtIsNull(TestFixtures.COMPANY_ID, pageable))
                .willReturn(new PageImpl<>(List.of(TestFixtures.companyManager())));

        // when
        PageRes<CompanyManagerResponse> result = companyManagerService.getList(TestFixtures.COMPANY_ID, pageable);

        // then
        assertThat(result).isNotNull();
        then(companyManagerRepository).should()
                .findAllByCompanyIdAndDeletedAtIsNull(TestFixtures.COMPANY_ID, pageable);
    }

    @Test
    @DisplayName("업체 담당자 단건 조회 성공")
    void getOne_success() {
        // given
        CompanyManager cm = TestFixtures.companyManager();
        given(companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(TestFixtures.COMPANY_MANAGER_ID))
                .willReturn(Optional.of(cm));

        // when
        CompanyManagerResponse response = companyManagerService.getOne(TestFixtures.COMPANY_MANAGER_ID);

        // then
        assertThat(response.getCompanyManagerId()).isEqualTo(TestFixtures.COMPANY_MANAGER_ID);
    }

    @Test
    @DisplayName("업체 담당자 단건 조회 실패 - 없는 ID")
    void getOne_notFound() {
        // given
        given(companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(any()))
                .willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> companyManagerService.getOne(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.COMPANY_MANAGER_NOT_FOUND));
    }

    @Test
    @DisplayName("업체 담당자 삭제 성공")
    void delete_success() {
        // given
        CompanyManager cm = TestFixtures.companyManager();
        given(companyManagerRepository.findByCompanyManagerIdAndDeletedAtIsNull(TestFixtures.COMPANY_MANAGER_ID))
                .willReturn(Optional.of(cm));

        // when
        companyManagerService.delete(TestFixtures.COMPANY_MANAGER_ID, "admin");

        // then
        assertThat(cm.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("업체 소속 여부 확인")
    void existsByUserIdAndCompanyId() {
        // given
        given(companyManagerRepository.existsByUserIdAndCompanyIdAndDeletedAtIsNull(
                TestFixtures.USER_ID, TestFixtures.COMPANY_ID)).willReturn(true);

        // when & then
        assertThat(companyManagerService.existsByUserIdAndCompanyId(
                TestFixtures.USER_ID, TestFixtures.COMPANY_ID)).isTrue();
    }

    private CompanyManagerRequest companyManagerRequest(UUID userId, UUID companyId) {
        CompanyManagerRequest req = new CompanyManagerRequest();
        ReflectionTestUtils.setField(req, "userId", userId);
        ReflectionTestUtils.setField(req, "companyId", companyId);
        return req;
    }

    private CompanyExistsResponse companyExistsResponse(boolean exists) {
        CompanyExistsResponse res = new CompanyExistsResponse();
        ReflectionTestUtils.setField(res, "exists", exists);
        return res;
    }
}
