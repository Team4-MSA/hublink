package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.dto.LogInRequest;
import com.msa.user_service.dto.LogInResponse;
import com.msa.user_service.entity.User;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.util.JwtUtil;
import com.msa.user_service.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private RedisUtil redisUtil;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공")
    void logIn_success() {
        // given
        User user = TestFixtures.approvedMasterUser();
        LogInRequest request = loginRequest("masteruser", "Password1!");

        given(userService.findUserForAuth("masteruser")).willReturn(user);
        given(passwordEncoder.matches("Password1!", user.getPassword())).willReturn(true);
        given(jwtUtil.generateAccessToken(any(), any(), any(), any())).willReturn("access-token");
        given(jwtUtil.generateRefreshToken(any())).willReturn("refresh-token");

        // when
        LogInResponse response = authService.logIn(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserId()).isEqualTo(TestFixtures.USER_ID);
        then(redisUtil).should().saveRefreshToken(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void logIn_passwordMismatch() {
        // given
        User user = TestFixtures.approvedMasterUser();
        LogInRequest request = loginRequest("masteruser", "WrongPassword!");

        given(userService.findUserForAuth("masteruser")).willReturn(user);
        given(passwordEncoder.matches("WrongPassword!", user.getPassword())).willReturn(false);

        // then
        assertThatThrownBy(() -> authService.logIn(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.PASSWORD_MISMATCH));
    }

    @Test
    @DisplayName("로그인 실패 - 미승인 계정")
    void logIn_notApproved() {
        // given
        User user = TestFixtures.pendingHubManagerUser();
        LogInRequest request = loginRequest("hubmanager1", "Password1!");

        given(userService.findUserForAuth("hubmanager1")).willReturn(user);
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        // then
        assertThatThrownBy(() -> authService.logIn(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.NOT_APPROVED));
    }

    @Test
    @DisplayName("로그아웃 성공 - AT 블랙리스트 등록 + RT 삭제")
    void logOut_success() {
        // given
        String accessToken = "valid-access-token";
        String userId = TestFixtures.USER_ID.toString();
        given(jwtUtil.getExpiration(accessToken)).willReturn(3600000L);

        // when
        authService.logOut(accessToken, userId);

        // then
        then(redisUtil).should().addBlacklist(accessToken, 3600000L);
        then(redisUtil).should().deleteRefreshToken(userId);
    }

    @Test
    @DisplayName("로그아웃 - AT 이미 만료된 경우 블랙리스트 등록 생략")
    void logOut_expiredToken_skipsBlacklist() {
        // given
        String accessToken = "expired-token";
        String userId = TestFixtures.USER_ID.toString();
        given(jwtUtil.getExpiration(accessToken)).willReturn(-1L);

        // when
        authService.logOut(accessToken, userId);

        // then
        then(redisUtil).should(never()).addBlacklist(anyString(), any(long.class));
        then(redisUtil).should().deleteRefreshToken(userId);
    }

    @Test
    @DisplayName("토큰 갱신 성공 - RTR 방식으로 새 AT/RT 발급")
    void refresh_success() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String userId = TestFixtures.USER_ID.toString();
        User user = TestFixtures.approvedMasterUser();

        given(jwtUtil.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtUtil.getUserId(oldRefreshToken)).willReturn(userId);
        given(redisUtil.getRefreshToken(userId)).willReturn(oldRefreshToken);
        given(userService.findActiveUserById(TestFixtures.USER_ID)).willReturn(user);
        given(jwtUtil.generateAccessToken(any(), any(), any(), any())).willReturn("new-access-token");
        given(jwtUtil.generateRefreshToken(any())).willReturn("new-refresh-token");

        // when
        LogInResponse response = authService.refresh(oldRefreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        then(redisUtil).should().saveRefreshToken(userId, "new-refresh-token");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Redis에 RT 없음 (로그아웃 상태)")
    void refresh_noStoredToken() {
        // given
        String refreshToken = "some-refresh-token";
        String userId = TestFixtures.USER_ID.toString();

        given(jwtUtil.validateToken(refreshToken)).willReturn(true);
        given(jwtUtil.getUserId(refreshToken)).willReturn(userId);
        given(redisUtil.getRefreshToken(userId)).willReturn(null);

        // then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.LOGIN_REQUIRED));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - RT 불일치 (탈취 감지 → RT 즉시 삭제)")
    void refresh_tokenMismatch_deletesStoredToken() {
        // given
        String requestToken = "request-refresh-token";
        String storedToken  = "stored-different-token";
        String userId       = TestFixtures.USER_ID.toString();

        given(jwtUtil.validateToken(requestToken)).willReturn(true);
        given(jwtUtil.getUserId(requestToken)).willReturn(userId);
        given(redisUtil.getRefreshToken(userId)).willReturn(storedToken);

        // then
        assertThatThrownBy(() -> authService.refresh(requestToken))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_TOKEN));

        then(redisUtil).should().deleteRefreshToken(userId);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 계정 비활성화 상태")
    void refresh_userNotApproved() {
        // given
        String refreshToken = "valid-refresh-token";
        String userId = TestFixtures.USER_ID.toString();
        User inactiveUser = TestFixtures.pendingHubManagerUser();

        given(jwtUtil.validateToken(refreshToken)).willReturn(true);
        given(jwtUtil.getUserId(refreshToken)).willReturn(userId);
        given(redisUtil.getRefreshToken(userId)).willReturn(refreshToken);
        given(userService.findActiveUserById(TestFixtures.USER_ID)).willReturn(inactiveUser);

        // then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.NOT_APPROVED));

        then(redisUtil).should().deleteRefreshToken(userId);
    }

    private LogInRequest loginRequest(String username, String password) {
        LogInRequest req = new LogInRequest();
        ReflectionTestUtils.setField(req, "username", username);
        ReflectionTestUtils.setField(req, "password", password);
        return req;
    }
}
