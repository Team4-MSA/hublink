package com.msa.hub_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.response.paging.PageRes;
import com.msa.hub_service.dto.HubRequest;
import com.msa.hub_service.dto.HubResponse;
import com.msa.hub_service.service.HubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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

@WebMvcTest(HubController.class)
class HubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HubService hubService;

    private final UUID HUB_ID = UUID.randomUUID();
    private final String HUB_NAME = "서울 중앙 허브";
    private final String HUB_ADDRESS = "서울시 강남구 테헤란로";
    private final BigDecimal LATITUDE = new BigDecimal("37.5");
    private final BigDecimal LONGITUDE = new BigDecimal("127.0");

    @Test
    @DisplayName("성공: 허브 생성 (MASTER 권한)")
    void createHub_Success() throws Exception {
        HubRequest request = new HubRequest(HUB_NAME, HUB_ADDRESS, LATITUDE, LONGITUDE);
        HubResponse expectedResponse = new HubResponse(HUB_ID, HUB_NAME, HUB_ADDRESS, LATITUDE, LONGITUDE);

        given(hubService.createHub(any(), any(), any(), any())).willReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/hubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubId").value(HUB_ID.toString()))
                .andExpect(jsonPath("$.data.name").value(HUB_NAME))
                .andExpect(jsonPath("$.data.address").value(HUB_ADDRESS));
    }

    @Test
    @DisplayName("성공: 허브 단건 상세 조회")
    void getHub_Success() throws Exception {
        HubResponse expectedResponse = new HubResponse(HUB_ID, HUB_NAME, HUB_ADDRESS, LATITUDE, LONGITUDE);

        given(hubService.getHub(any())).willReturn(expectedResponse);

        mockMvc.perform(get("/api/v1/hubs/{hubId}", HUB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubId").value(HUB_ID.toString()))
                .andExpect(jsonPath("$.data.name").value(HUB_NAME));
    }

    @Test
    @DisplayName("성공: 허브 수정 (MASTER 권한)")
    void updateHub_Success() throws Exception {
        String updateName = "부산 중앙 허브";
        String updateAddress = "부산시 해운대구";
        BigDecimal updateLat = new BigDecimal("35.1");
        BigDecimal updateLon = new BigDecimal("129.1");

        HubRequest request = new HubRequest(updateName, updateAddress, updateLat, updateLon);
        HubResponse expectedResponse = new HubResponse(HUB_ID, updateName, updateAddress, updateLat, updateLon);

        given(hubService.updateHub(any(), any())).willReturn(expectedResponse);

        mockMvc.perform(patch("/api/v1/hubs/{hubId}", HUB_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(updateName))
                .andExpect(jsonPath("$.data.address").value(updateAddress));
    }

    @Test
    @DisplayName("성공: 조건에 맞는 허브 목록 페이징 검색")
    void getHubs_Success() throws Exception {
        HubResponse response1 = new HubResponse(UUID.randomUUID(), "서울 허브", "서울", LATITUDE, LONGITUDE);
        HubResponse response2 = new HubResponse(UUID.randomUUID(), "서울 외곽 허브", "경기", LATITUDE, LONGITUDE);

        PageImpl<HubResponse> page = new PageImpl<>(List.of(response1, response2), PageRequest.of(0, 10), 2);
        PageRes<HubResponse> expectedResponse = new PageRes<>(page);

        given(hubService.getHubs(any(), any())).willReturn(expectedResponse);

        mockMvc.perform(get("/api/v1/hubs")
                        .param("name", "서울")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("서울 허브"))
                .andExpect(jsonPath("$.data.content[1].name").value("서울 외곽 허브"));
    }

    @Test
    @DisplayName("성공: 허브 삭제 (MASTER 권한)")
    void deleteHub_Success() throws Exception {
        HubResponse expectedResponse = new HubResponse(HUB_ID, HUB_NAME, HUB_ADDRESS, LATITUDE, LONGITUDE);

        given(hubService.deleteHub(any())).willReturn(expectedResponse);

        mockMvc.perform(delete("/api/v1/hubs/{hubId}", HUB_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubId").value(HUB_ID.toString()));
    }
}