package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.config.SecurityConfig;
import com.msa.user_service.dto.HubManagerResponse;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.GlobalExceptionHandler;
import com.msa.user_service.service.HubManagerService;
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

@WebMvcTest(HubManagerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("HubManagerController 테스트")
class HubManagerControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean HubManagerService hubManagerService;

    @Test
    @DisplayName("허브 매니저 등록 성공 - MASTER, 201 Created")
    void register_asMaster() throws Exception {
        // given
        HubManagerResponse response = TestFixtures.hubManagerResponse();
        given(hubManagerService.register(any())).willReturn(response);

        String body = """
                {
                    "userId": "%s",
                    "hubId": "%s"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.HUB_ID);

        // when & then
        mockMvc.perform(post("/api/v1/hub-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.hubManagerId").value(TestFixtures.HUB_MANAGER_ID.toString()));
    }

    @Test
    @DisplayName("허브 매니저 등록 - MASTER 아닌 경우 → 403")
    void register_forbidden() throws Exception {
        // given
        String body = """
                {
                    "userId": "%s",
                    "hubId": "%s"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.HUB_ID);

        // when & then
        mockMvc.perform(post("/api/v1/hub-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("허브 매니저 목록 조회 - MASTER만 가능")
    void getList_asMaster() throws Exception {
        // given
        PageRes<HubManagerResponse> page = new PageRes<>(
                new PageImpl<>(List.of(TestFixtures.hubManagerResponse())));
        given(hubManagerService.getList(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/hub-managers")
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("허브 매니저 목록 조회 - MASTER 아닌 경우 → 403")
    void getList_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/hub-managers")
                        .header("X-User-Role", "DELIVERY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("허브 매니저 단건 조회 - MASTER 가능")
    void getOne_asMaster() throws Exception {
        // given
        HubManagerResponse response = TestFixtures.hubManagerResponse();
        given(hubManagerService.getOne(TestFixtures.HUB_MANAGER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hub-managers/{hubManagerId}", TestFixtures.HUB_MANAGER_ID)
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(TestFixtures.USER_ID.toString()));
    }

    @Test
    @DisplayName("허브 매니저 단건 조회 - HUB_MANAGER도 가능")
    void getOne_asHubManager() throws Exception {
        // given
        HubManagerResponse response = TestFixtures.hubManagerResponse();
        given(hubManagerService.getOne(TestFixtures.HUB_MANAGER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hub-managers/{hubManagerId}", TestFixtures.HUB_MANAGER_ID)
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("허브 매니저 단건 조회 - COMPANY_MANAGER → 403")
    void getOne_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/hub-managers/{hubManagerId}", TestFixtures.HUB_MANAGER_ID)
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("허브 매니저 삭제 - MASTER만 가능, 204 No Content")
    void delete_asMaster() throws Exception {
        // given
        willDoNothing().given(hubManagerService).delete(any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/hub-managers/{hubManagerId}", TestFixtures.HUB_MANAGER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("허브 매니저 삭제 - MASTER 아닌 경우 → 403")
    void delete_forbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/hub-managers/{hubManagerId}", TestFixtures.HUB_MANAGER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }
}
