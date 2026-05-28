package com.msa.hub_service.controller;

import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.entity.RouteType;
import com.msa.hub_service.service.HubRouteService;
import com.msa.hub_service.service.HubService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalHubController.class)
class InternalHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HubService hubService;

    @MockitoBean
    private HubRouteService hubRouteService;

    private final UUID HUB_ID = UUID.randomUUID();
    private final UUID DEP_COMPANY_ID = UUID.randomUUID();
    private final UUID ARR_COMPANY_ID = UUID.randomUUID();
    private final BigDecimal LATITUDE = new BigDecimal("37.5");
    private final BigDecimal LONGITUDE = new BigDecimal("127.0");
    private final String DEP_HUB_NAME = "출발 허브 센터";
    private final String ARR_HUB_NAME = "도착 허브 센터";
    private final String ARR_COMP_NAME = "도착 업체명";

    @Test
    @DisplayName("성공: 허브 존재 여부 확인 (Internal)")
    void getHubExist_Success() throws Exception {
        // given
        given(hubService.getHubExist(any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/internal/hubs/{hubId}/exists", HUB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("성공: 주소 기반 좌표 조회 (Internal)")
    void getCoordinates_Success() throws Exception {
        // given
        String address = "서울시 강남구 테헤란로";
        CoordinateDto expectedResponse = new CoordinateDto(LATITUDE, LONGITUDE);

        given(hubService.getCoordinate(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/internal/hubs/coordinates")
                        .param("address", address)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.5))
                .andExpect(jsonPath("$.longitude").value(127.0));
    }

    @Test
    @DisplayName("성공: 업체 간 배송 최적 경로 탐색 (Internal)")
    void getCompanyPath_Success() throws Exception {
        // given
        UUID depHubId = UUID.randomUUID();
        UUID arrHubId = UUID.randomUUID();

        HubRouteResponse path1 = new HubRouteResponse(
                UUID.randomUUID(), depHubId, arrHubId, DEP_COMPANY_ID,
                DEP_HUB_NAME, ARR_HUB_NAME, null,
                new BigDecimal("50.0"), 30, RouteType.H2H, 1
        );
        HubRouteResponse path2 = new HubRouteResponse(
                UUID.randomUUID(), depHubId, arrHubId, ARR_COMPANY_ID,
                DEP_HUB_NAME, ARR_HUB_NAME, ARR_COMP_NAME,
                new BigDecimal("60.0"), 40, RouteType.H2H, 2
        );

        List<HubRouteResponse> expectedResponse = List.of(path1, path2);

        given(hubRouteService.getCompanyToCompanyPath(any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/internal/hub-routes/path")
                        .param("departureCompanyId", DEP_COMPANY_ID.toString())
                        .param("arrivalCompanyId", ARR_COMPANY_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].arrivalCompanyId").value(DEP_COMPANY_ID.toString()))
                .andExpect(jsonPath("$[0].departureHubName").value(DEP_HUB_NAME)) // 🌟 이름 검증
                .andExpect(jsonPath("$[0].sequence").value(1))
                .andExpect(jsonPath("$[1].arrivalCompanyId").value(ARR_COMPANY_ID.toString()))
                .andExpect(jsonPath("$[1].arrivalCompanyName").value(ARR_COMP_NAME)) // 🌟 이름 검증
                .andExpect(jsonPath("$[1].sequence").value(2));
    }
}