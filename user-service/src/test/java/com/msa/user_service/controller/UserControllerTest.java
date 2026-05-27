package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.config.SecurityConfig;
import com.msa.user_service.dto.UserResponse;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.GlobalExceptionHandler;
import com.msa.user_service.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;

    @Test
    @DisplayName("내 정보 조회 성공 - 200 OK")
    void getMe_success() throws Exception {
        // given
        UserResponse response = TestFixtures.userResponse();
        given(userService.getUser(TestFixtures.USER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-User-Id", TestFixtures.USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("masteruser"));
    }

    @Test
    @DisplayName("유저 단건 조회 - MASTER는 다른 유저도 조회 가능")
    void getUser_asMaster() throws Exception {
        // given
        UserResponse response = TestFixtures.userResponse();
        given(userService.getUser(TestFixtures.USER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 단건 조회 - 본인은 본인 조회 가능")
    void getUser_selfAccess() throws Exception {
        // given
        UserResponse response = TestFixtures.userResponse();
        given(userService.getUser(TestFixtures.USER_ID)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 단건 조회 - 타인 접근 → 403")
    void getUser_accessDenied() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString()) // 다른 사람
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("전체 유저 목록 조회 - MASTER만 가능")
    void getUsers_asMaster() throws Exception {
        // given
        PageRes<UserResponse> page = new PageRes<>(
                new PageImpl<>(List.of(TestFixtures.userResponse())));
        given(userService.getUsers(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/users")
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전체 유저 목록 조회 - MASTER 아닌 경우 → 403")
    void getUsers_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users")
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PENDING 유저 목록 조회 - MASTER만 가능")
    void getPendingUsers_asMaster() throws Exception {
        // given
        PageRes<UserResponse> page = new PageRes<>(new PageImpl<>(List.of()));
        given(userService.getPendingUsers(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/users/pending")
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 정보 수정 성공 - MASTER")
    void updateUser_asMaster() throws Exception {
        // given
        UserResponse response = TestFixtures.userResponse();
        given(userService.updateUser(eq(TestFixtures.USER_ID), any())).willReturn(response);

        String body = """
                {
                    "name": "변경이름",
                    "email": "changed@example.com",
                    "slackId": "U_NEW"
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 정보 수정 - MASTER 아닌 경우 → 403 (타인 수정)")
    void updateUser_otherUser_forbidden() throws Exception {
        String body = """
                {
                    "name": "변경이름",
                    "email": "changed@example.com",
                    "slackId": "U_NEW"
                }
                """;

        mockMvc.perform(put("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 정보 수정 - MASTER 아닌 경우 → 403 (본인도 수정 불가)")
    void updateUser_selfButNotMaster_forbidden() throws Exception {
        String body = """
                {
                    "name": "변경이름",
                    "email": "changed@example.com",
                    "slackId": "U_NEW"
                }
                """;

        mockMvc.perform(put("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.USER_ID.toString()) // 본인이지만
                        .header("X-User-Role", "DELIVERY_MANAGER"))           // MASTER 아님
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 정보 수정 - hubId 포함 - MASTER 가능")
    void updateUser_withHubId_asMaster() throws Exception {
        // given
        UserResponse response = TestFixtures.userResponse();
        given(userService.updateUser(eq(TestFixtures.USER_ID), any())).willReturn(response);

        String body = """
                {
                    "name": "변경이름",
                    "hubId": "%s"
                }
                """.formatted(TestFixtures.HUB_ID);

        // when & then
        mockMvc.perform(put("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 삭제 성공 - MASTER만 가능, 204 No Content")
    void deleteUser_asMaster() throws Exception {
        // given
        willDoNothing().given(userService).deleteUser(any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("유저 삭제 - MASTER 아닌 경우 → 403")
    void deleteUser_forbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/users/{userId}", TestFixtures.USER_ID)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 상태 변경(승인/거절) - MASTER만 가능, 200 OK")
    void approveUser_asMaster() throws Exception {
        // given
        willDoNothing().given(userService).approveUser(any(), any(), any());

        String body = """
                {
                    "status": "APPROVED"
                }
                """;

        // when & then
        mockMvc.perform(patch("/api/v1/users/{userId}/status", TestFixtures.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.ADMIN_ID.toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 상태 변경 - MASTER 아닌 경우 → 403")
    void approveUser_forbidden() throws Exception {
        // given
        String body = """
                {
                    "status": "APPROVED"
                }
                """;

        // when & then
        mockMvc.perform(patch("/api/v1/users/{userId}/status", TestFixtures.USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", TestFixtures.USER_ID.toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isForbidden());
    }
}
