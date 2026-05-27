package com.msa.company_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.company_service.dto.CompanyDto;
import com.msa.company_service.dto.CompanyNameResponse;
import com.msa.company_service.service.CompanyService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalCompanyController.class)
class InternalCompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyService companyService;
    
    private final UUID COMPANY_ID = UUID.randomUUID();
    private final UUID HUB_ID = UUID.randomUUID();
    private final BigDecimal LAT = new BigDecimal("37.5");
    private final BigDecimal LON = new BigDecimal("127.0");

    @Test
    @DisplayName("성공: 업체 위치 정보 조회 (Internal)")
    void getCompanyLocation_Success() throws Exception {
        // given
        CompanyDto expectedResponse = new CompanyDto("서울시 강남구", LAT, LON, HUB_ID);

        given(companyService.getCompanyLoc(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/internal/companies/{companyId}/location", COMPANY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.latitude").value(37.5))
                .andExpect(jsonPath("$.longitude").value(127.0));
    }

    @Test
    @DisplayName("성공: 업체 존재 여부 확인 (Internal)")
    void checkCompanyExists_Success() throws Exception {
        // given
        given(companyService.getCompanyExists(any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/internal/companies/{companyId}/exists", COMPANY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("성공: 다중 업체 이름 조회 (Internal)")
    void getCompanyNames_Success() throws Exception {
        // given
        UUID companyId1 = UUID.randomUUID();
        UUID companyId2 = UUID.randomUUID();

        CompanyNameResponse response1 = new CompanyNameResponse(companyId1, "업체 A");
        CompanyNameResponse response2 = new CompanyNameResponse(companyId2, "업체 B");

        List<CompanyNameResponse> expectedResponse = List.of(response1, response2);

        given(companyService.getCompanyNames(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/internal/companies/names")
                        .param("companyIds", companyId1.toString(), companyId2.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(companyId1.toString()))
                .andExpect(jsonPath("$[0].name").value("업체 A"))
                .andExpect(jsonPath("$[1].id").value(companyId2.toString()))
                .andExpect(jsonPath("$[1].name").value("업체 B"));
    }
}