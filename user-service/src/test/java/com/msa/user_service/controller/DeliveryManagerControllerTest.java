package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.config.SecurityConfig;
import com.msa.user_service.dto.DeliveryManagerResponse;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.GlobalExceptionHandler;
import com.msa.user_service.service.DeliveryManagerService;
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

@WebMvcTest(DeliveryManagerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("DeliveryManagerController 테스트")
class DeliveryManagerControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean DeliveryManagerService deliveryManagerService;

    @Test
    @DisplayName("배송 담당자 등록 성공 - MASTER, 201 Created")
    void register_asMaster() throws Exception {
        // given
        DeliveryManagerResponse response = TestFixtures.deliveryManagerResponse();
        given(deliveryManagerService.register(any(), any(), any())).willReturn(response);

        String body = """
                {
                    "userId": "%s",
                    "hubId": "%s",
                    "type": "HUB_DELIVERY"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.HUB_ID);

        // when & then
        mockMvc.perform(post("/api/v1/delivery-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(TestFixtures.USER_ID.toString()))
                .andExpect(jsonPath("$.data.type").value("HUB_DELIVERY"));
    }

    @Test
    @DisplayName("배송 담당자 등록 - HUB_MANAGER도 가능")
    void register_asHubManager() throws Exception {
        // given
        DeliveryManagerResponse response = TestFixtures.deliveryManagerResponse();
        given(deliveryManagerService.register(any(), any(), any())).willReturn(response);

        String body = """
                {
                    "userId": "%s",
                    "hubId": "%s",
                    "type": "HUB_DELIVERY"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.HUB_ID);

        // when & then
        mockMvc.perform(post("/api/v1/delivery-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("배송 담당자 등록 - 권한 없는 경우 → 403")
    void register_forbidden() throws Exception {
        // given
        String body = """
                {
                    "userId": "%s",
                    "hubId": "%s",
                    "type": "HUB_DELIVERY"
                }
                """.formatted(TestFixtures.USER_ID, TestFixtures.HUB_ID);

        // when & then
        mockMvc.perform(post("/api/v1/delivery-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("배송 담당자 목록 조회 - MASTER 가능")
    void getList_asMaster() throws Exception {
        // given
        PageRes<DeliveryManagerResponse> page = new PageRes<>(
                new PageImpl<>(List.of(TestFixtures.deliveryManagerResponse())));
        given(deliveryManagerService.getList(any(), any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/delivery-managers")
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("배송 담당자 목록 조회 - 권한 없는 경우 → 403")
    void getList_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/delivery-managers")
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("배송 담당자 단건 조회 - MASTER 가능")
    void getOne_asMaster() throws Exception {
        // given
        DeliveryManagerResponse response = TestFixtures.deliveryManagerResponse();
        given(deliveryManagerService.getOne(any(), any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/delivery-managers/{targetUserId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hubId").value(TestFixtures.HUB_ID.toString()));
    }

    @Test
    @DisplayName("배송 담당자 단건 조회 - DELIVERY_MANAGER 자신도 가능")
    void getOne_asDeliveryManager() throws Exception {
        // given
        DeliveryManagerResponse response = TestFixtures.deliveryManagerResponse();
        given(deliveryManagerService.getOne(any(), any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/delivery-managers/{targetUserId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "DELIVERY_MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("배송 담당자 단건 조회 - COMPANY_MANAGER → 403")
    void getOne_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/delivery-managers/{targetUserId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("배송 담당자 삭제 - MASTER 가능, 204 No Content")
    void delete_asMaster() throws Exception {
        // given
        willDoNothing().given(deliveryManagerService).delete(any(), any(), any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/delivery-managers/{targetUserId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("배송 담당자 삭제 - COMPANY_MANAGER → 403")
    void delete_forbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/delivery-managers/{targetUserId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }
}
