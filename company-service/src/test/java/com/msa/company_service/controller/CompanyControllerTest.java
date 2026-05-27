package com.msa.company_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.company_service.dto.CompanyRequest;
import com.msa.company_service.dto.CompanyResponse;
import com.msa.company_service.dto.CompanyUpdateRequest;
import com.msa.company_service.entity.CompanyType;
import com.msa.company_service.service.CompanyService;
import com.msa.core_common.auth.UserRole;
import com.msa.core_common.response.paging.PageRes;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyService companyService;

    private final UUID COMPANY_ID = UUID.randomUUID();
    private final UUID HUB_ID = UUID.randomUUID();
    private final UUID USER_HUB_ID = UUID.randomUUID();
    private final UUID USER_COMPANY_ID = UUID.randomUUID();
    private final String NAME = "테스트 업체";
    private final String ADDRESS = "서울시 강남구 테헤란로";
    private final BigDecimal LAT = new BigDecimal("37.5");
    private final BigDecimal LON = new BigDecimal("127.0");

    @Test
    @DisplayName("성공: 업체 생성 (MASTER 또는 HUB_MANAGER 권한)")
    void createCompany_Success() throws Exception {
        // given
        CompanyRequest request = new CompanyRequest(HUB_ID, NAME, CompanyType.SUPPLIER, ADDRESS, LAT, LON);
        CompanyResponse expectedResponse = new CompanyResponse(
                COMPANY_ID, HUB_ID, NAME, CompanyType.SUPPLIER, ADDRESS, LAT, LON
        );

        given(companyService.createCompany(any(), any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/v1/companies")
                        .header("X-User-Role", UserRole.MASTER.name())
                        .header("X-Hub-Id", USER_HUB_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.data.name").value(NAME))
                .andExpect(jsonPath("$.data.type").value(CompanyType.SUPPLIER.name()));
    }

    @Test
    @DisplayName("성공: 단건 조회 (권한 무관)")
    void getCompany_Success() throws Exception {
        // given
        CompanyResponse expectedResponse = new CompanyResponse(
                COMPANY_ID, HUB_ID, NAME, CompanyType.SUPPLIER, ADDRESS, LAT, LON
        );

        given(companyService.getCompany(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", COMPANY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.data.name").value(NAME));
    }

    @Test
    @DisplayName("성공: 업체 수정 (다양한 권한 및 헤더 처리)")
    void updateCompany_Success() throws Exception {
        // given
        CompanyUpdateRequest request = new CompanyUpdateRequest(
                HUB_ID, "수정된 업체", CompanyType.RECEIVER, "수정된 주소", LAT, LON
        );
        CompanyResponse expectedResponse = new CompanyResponse(
                COMPANY_ID, HUB_ID, "수정된 업체", CompanyType.RECEIVER, "수정된 주소", LAT, LON
        );

        given(companyService.updateCompany(any(), any(), any(), any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(patch("/api/v1/companies/{companyId}", COMPANY_ID)
                        .header("X-User-Role", UserRole.COMPANY_MANAGER.name())
                        .header("X-Company-Id", USER_COMPANY_ID.toString())
                        .header("X-Hub-Id", USER_HUB_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 업체"))
                .andExpect(jsonPath("$.data.address").value("수정된 주소"));
    }

    @Test
    @DisplayName("성공: 업체 삭제 (MASTER 또는 HUB_MANAGER 권한)")
    void deleteCompany_Success() throws Exception {
        // given
        CompanyResponse expectedResponse = new CompanyResponse(
                COMPANY_ID, HUB_ID, NAME, CompanyType.SUPPLIER, ADDRESS, LAT, LON
        );

        given(companyService.deleteCompany(any(), any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(delete("/api/v1/companies/{companyId}", COMPANY_ID)
                        .header("X-User-Role", UserRole.HUB_MANAGER.name())
                        .header("X-Hub-Id", USER_HUB_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(COMPANY_ID.toString()));
    }

    @Test
    @DisplayName("성공: 업체 다건/페이징 검색")
    void getCompanies_Success() throws Exception {
        // given
        CompanyResponse response1 = new CompanyResponse(UUID.randomUUID(), HUB_ID, "업체1", CompanyType.SUPPLIER, ADDRESS, LAT, LON);
        CompanyResponse response2 = new CompanyResponse(UUID.randomUUID(), HUB_ID, "업체2", CompanyType.RECEIVER, ADDRESS, LAT, LON);

        PageImpl<CompanyResponse> page = new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 10), 2);
        PageRes<CompanyResponse> expectedResponse = new PageRes<>(page);

        given(companyService.getCompanies(any(), any(), any(), any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/companies")
                        .param("hubId", HUB_ID.toString())
                        .param("name", "업체")
                        .param("type", CompanyType.SUPPLIER.name())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("업체1"))
                .andExpect(jsonPath("$.data.content[1].name").value("업체2"));
    }

    @Test
    @DisplayName("성공: 내 담당 업체 조회 (COMPANY_MANAGER 전용)")
    void getMyCompany_Success() throws Exception {
        // given
        CompanyResponse expectedResponse = new CompanyResponse(
                USER_COMPANY_ID, HUB_ID, "내 업체", CompanyType.SUPPLIER, ADDRESS, LAT, LON
        );

        given(companyService.getCompany(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/companies/me")
                        .header("X-Company-Id", USER_COMPANY_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(USER_COMPANY_ID.toString()))
                .andExpect(jsonPath("$.data.name").value("내 업체"));
    }
}