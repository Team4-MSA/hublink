package com.msa.user_service.controller;

import com.msa.user_service.config.SecurityConfig;
import com.msa.user_service.dto.LogInResponse;
import com.msa.user_service.dto.UserResponse;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.GlobalExceptionHandler;
import com.msa.user_service.global.UserErrorCode;
import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.service.AuthService;
import com.msa.user_service.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthService authService;
    @MockitoBean UserService userService;

    @Test
    @DisplayName("회원가입 성공 - 201 Created")
    void signUp_success() throws Exception {
        // given
        UserResponse userResponse = TestFixtures.userResponse();
        given(userService.signUp(any())).willReturn(userResponse);

        String body = """
                {
                    "username": "newuser1",
                    "password": "Password1!",
                    "name": "새유저",
                    "email": "new@example.com",
                    "slackId": "U_NEW",
                    "role": "MASTER"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("masteruser"));
    }

    @Test
    @DisplayName("회원가입 실패 - username 형식 오류 (4~10자 소문자/숫자) → 400")
    void signUp_invalidUsername() throws Exception {
        // given - 대문자 포함된 username
        String body = """
                {
                    "username": "BadUser",
                    "password": "Password1!",
                    "name": "이름",
                    "email": "valid@example.com",
                    "slackId": "U_TEST",
                    "role": "MASTER"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 - 200 OK, 토큰 반환")
    void login_success() throws Exception {
        // given
        LogInResponse loginResponse = LogInResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .userId(TestFixtures.USER_ID)
                .username("masteruser")
                .role("MASTER")
                .build();
        given(authService.logIn(any())).willReturn(loginResponse);

        String body = """
                {
                    "username": "masteruser",
                    "password": "Password1!"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.role").value("MASTER"));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 → 400")
    void login_passwordMismatch() throws Exception {
        // given
        given(authService.logIn(any())).willThrow(new CustomException(UserErrorCode.PASSWORD_MISMATCH));

        String body = """
                {
                    "username": "masteruser",
                    "password": "WrongPass1!"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 200 OK")
    void refresh_success() throws Exception {
        // given
        LogInResponse loginResponse = LogInResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .userId(TestFixtures.USER_ID)
                .username("masteruser")
                .role("MASTER")
                .build();
        given(authService.refresh(anyString())).willReturn(loginResponse);

        String body = """
                {
                    "refreshToken": "old-refresh-token"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("로그아웃 성공 - 200 OK")
    void logout_success() throws Exception {
        // given
        willDoNothing().given(authService).logOut(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer some-access-token")
                        .header("X-User-Id", TestFixtures.USER_ID.toString()))
                .andExpect(status().isOk());
    }
}
