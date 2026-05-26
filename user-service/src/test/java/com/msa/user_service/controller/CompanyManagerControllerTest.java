package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.config.SecurityConfig;
import com.msa.user_service.dto.CompanyManagerResponse;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.GlobalExceptionHandler;
import com.msa.user_service.service.CompanyManagerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyManagerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("CompanyManagerController 테스트")
class CompanyManagerControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CompanyManagerService companyManagerService;

    @Test
    @DisplayName("업체 담당자 등록 성공 - MASTER, 201 Created")
    void register_asMaster() throws Exception {
        // given
        CompanyManagerResponse response = TestFixtures.companyManagerResponse();
        given(companyManagerService.register(any())).willReturn(response);

        String body = """
                {
                    "userId": "%s",
                    "companyId": "%s"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.COMPANY_ID);

        // when & then
        mockMvc.perform(post("/api/v1/company-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyManagerId").value(TestFixtures.COMPANY_MANAGER_ID.toString()));
    }

    @Test
    @DisplayName("업체 담당자 등록 - MASTER 아닌 경우 → 403")
    void register_forbidden() throws Exception {
        // given
        String body = """
                {
                    "userId": "%s",
                    "companyId": "%s"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.COMPANY_ID);

        // when & then
        mockMvc.perform(post("/api/v1/company-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("업체 담당자 목록 조회 - MASTER만 가능")
    void getList_asMaster() throws Exception {
        // given
        PageRes<CompanyManagerResponse> page = new PageRes<>(
                new PageImpl<>(List.of(TestFixtures.companyManagerResponse())));
        given(companyManagerService.getList(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/company-managers")
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("업체 담당자 목록 조회 - MASTER 아닌 경우 → 403")
    void getList_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/company-managers")
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("업체 담당자 단건 조회 - MASTER 가능")
    void getOne_asMaster() throws Exception {
        // given
        CompanyManagerResponse response = TestFixtures.companyManagerResponse();
        given(companyManagerService.getOne(TestFixtures.COMPANY_MANAGER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/company-managers/{companyManagerId}", TestFixtures.COMPANY_MANAGER_ID)
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(TestFixtures.USER_ID.toString()));
    }

    @Test
    @DisplayName("업체 담당자 단건 조회 - COMPANY_MANAGER도 가능")
    void getOne_asCompanyManager() throws Exception {
        // given
        CompanyManagerResponse response = TestFixtures.companyManagerResponse();
        given(companyManagerService.getOne(TestFixtures.COMPANY_MANAGER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/company-managers/{companyManagerId}", TestFixtures.COMPANY_MANAGER_ID)
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("업체 담당자 단건 조회 - HUB_MANAGER → 403")
    void getOne_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/company-managers/{companyManagerId}", TestFixtures.COMPANY_MANAGER_ID)
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("업체 담당자 삭제 - MASTER만 가능, 204 No Content")
    void delete_asMaster() throws Exception {
        // given
        willDoNothing().given(companyManagerService).delete(any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/company-managers/{companyManagerId}", TestFixtures.COMPANY_MANAGER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("업체 담당자 삭제 - MASTER 아닌 경우 → 403")
    void delete_forbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/company-managers/{companyManagerId}", TestFixtures.COMPANY_MANAGER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }
}
