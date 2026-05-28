package com.msa.hub_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.response.paging.PageRes;
import com.msa.hub_service.dto.HubRouteRequest;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.dto.HubRouteUpdateRequest;
import com.msa.hub_service.entity.RouteType;
import com.msa.hub_service.service.HubRouteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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


@WebMvcTest(HubRouteController.class)
class HubRouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HubRouteService hubRouteService;

    private final UUID ROUTE_ID = UUID.randomUUID();
    private final UUID DEP_HUB_ID = UUID.randomUUID();
    private final UUID ARR_HUB_ID = UUID.randomUUID();
    private final UUID COMPANY_ID = UUID.randomUUID();
    private final BigDecimal DISTANCE = new BigDecimal("150.5");
    private final Integer DURATION = 120;

    @Test
    @DisplayName("성공: 허브 경로 등록 (MASTER 권한)")
    void createHubRoute_Success() throws Exception {
        // given
        HubRouteRequest request = new HubRouteRequest(DEP_HUB_ID, ARR_HUB_ID);

        HubRouteResponse expectedResponse = new HubRouteResponse(
                ROUTE_ID, DEP_HUB_ID, ARR_HUB_ID, null, DISTANCE, DURATION, RouteType.H2H, null
        );

        given(hubRouteService.createHubRoute(any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/v1/hub-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubRouteId").value(ROUTE_ID.toString()))
                .andExpect(jsonPath("$.data.departureHub").value(DEP_HUB_ID.toString()))
                .andExpect(jsonPath("$.data.arrivalHub").value(ARR_HUB_ID.toString()))
                .andExpect(jsonPath("$.data.routeType").value(RouteType.H2H.name()));
    }

    @Test
    @DisplayName("성공: 허브 경로 단건 상세 조회")
    void getHubRoute_Success() throws Exception {
        // given
        HubRouteResponse expectedResponse = new HubRouteResponse(
                ROUTE_ID, DEP_HUB_ID, ARR_HUB_ID, null, DISTANCE, DURATION, RouteType.P2P, null
        );

        given(hubRouteService.getHubRoute(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/hub-routes/{hubRouteId}", ROUTE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubRouteId").value(ROUTE_ID.toString()))
                .andExpect(jsonPath("$.data.routeType").value(RouteType.P2P.name()));
    }

    @Test
    @DisplayName("성공: 허브 경로 정보 수정 (MASTER 권한)")
    void updateHubRoute_Success() throws Exception {
        // given
        BigDecimal newDistance = new BigDecimal("200.0");
        Integer newDuration = 150;

        HubRouteUpdateRequest request = new HubRouteUpdateRequest(newDistance, newDuration);

        HubRouteResponse expectedResponse = new HubRouteResponse(
                ROUTE_ID, DEP_HUB_ID, ARR_HUB_ID, null, newDistance, newDuration, RouteType.H2H, null
        );

        given(hubRouteService.updateHubRoute(any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(patch("/api/v1/hub-routes/{hubRouteId}", ROUTE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estimatedDistanceKm").value(200.0))
                .andExpect(jsonPath("$.data.estimatedDurationMin").value(150))
                .andExpect(jsonPath("$.data.routeType").value(RouteType.H2H.name()));
    }

    @Test
    @DisplayName("성공: 허브 경로 삭제 (MASTER 권한)")
    void deleteHubRoute_Success() throws Exception {
        // given
        HubRouteResponse expectedResponse = new HubRouteResponse(
                ROUTE_ID, DEP_HUB_ID, ARR_HUB_ID, null, DISTANCE, DURATION, RouteType.H2H, null
        );

        given(hubRouteService.deleteHubRoute(any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(delete("/api/v1/hub-routes/{hubRouteId}", ROUTE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubRouteId").value(ROUTE_ID.toString()));
    }

    @Test
    @DisplayName("성공: 출발/도착 허브 및 타입으로 경로 목록 페이징 검색")
    void getHubRoutes_Success() throws Exception {
        // given
        HubRouteResponse response1 = new HubRouteResponse(
                UUID.randomUUID(), DEP_HUB_ID, ARR_HUB_ID, null, new BigDecimal("100.0"), 60, RouteType.H2H, null
        );
        HubRouteResponse response2 = new HubRouteResponse(
                UUID.randomUUID(), DEP_HUB_ID, UUID.randomUUID(), null, new BigDecimal("200.0"), 120, RouteType.H2H, null
        );

        PageImpl<HubRouteResponse> page = new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 10), 2);
        PageRes<HubRouteResponse> expectedResponse = new PageRes<>(page);

        given(hubRouteService.getHubRoutes(any(), any(), any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/hub-routes")
                        .param("departureHubId", DEP_HUB_ID.toString())
                        .param("routeType", RouteType.H2H.name())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].estimatedDistanceKm").value(100.0))
                .andExpect(jsonPath("$.data.content[1].estimatedDistanceKm").value(200.0));
    }

    @Test
    @DisplayName("성공: 최적의 허브 경로(List) 탐색")
    void getHubPath_Success() throws Exception {
        // given
        UUID transitHubId = UUID.randomUUID();

        HubRouteResponse path1 = new HubRouteResponse(
                UUID.randomUUID(), DEP_HUB_ID, transitHubId, COMPANY_ID, new BigDecimal("50.0"), 30, RouteType.H2H, 1
        );
        HubRouteResponse path2 = new HubRouteResponse(
                UUID.randomUUID(), transitHubId, ARR_HUB_ID, COMPANY_ID, new BigDecimal("60.0"), 40, RouteType.H2H, 2
        );

        List<HubRouteResponse> expectedResponse = List.of(path1, path2);

        given(hubRouteService.getHubPath(any(), any(), any())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/hub-routes/path")
                        .param("departureHubId", DEP_HUB_ID.toString())
                        .param("arrivalHubId", ARR_HUB_ID.toString())
                        .param("companyId", COMPANY_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].departureHub").value(DEP_HUB_ID.toString()))
                .andExpect(jsonPath("$.data[0].arrivalHub").value(transitHubId.toString()))
                .andExpect(jsonPath("$.data[1].departureHub").value(transitHubId.toString()))
                .andExpect(jsonPath("$.data[1].arrivalHub").value(ARR_HUB_ID.toString()));
    }
}