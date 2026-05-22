package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.dto.LogInRequest;
import com.msa.user_service.dto.LogInResponse;
import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserStatus;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.util.JwtUtil;
import com.msa.user_service.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LogInResponse logIn(LogInRequest request) {

        // 1. User 엔티티 직접 조회 (password 검증 필요)
        User user = userService.findUserForAuth(request.getUsername());

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(UserErrorCode.PASSWORD_MISMATCH);
        }

        // 3. APPROVED 상태 확인
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new CustomException(UserErrorCode.NOT_APPROVED);
        }

        // 4. AT + RT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 5. Redis에 RT 저장
        redisUtil.saveRefreshToken(
                user.getUserId().toString(),
                refreshToken
        );

        return LogInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public void logOut(String accessToken, String userId) {

        // 1. AT 블랙리스트 등록
        long expiration = jwtUtil.getExpiration(accessToken);
        if (expiration > 0) {
            redisUtil.addBlacklist(accessToken, expiration);
        }

        // 2. RT 삭제
        redisUtil.deleteRefreshToken(userId);
    }

    public LogInResponse refresh(String refreshToken) {

        // 1. RT 서명/만료 검증 후 userId 추출 (클라이언트 값 신뢰하지 않음)
        jwtUtil.validateToken(refreshToken);
        String userId = jwtUtil.getUserId(refreshToken);

        // 2. Redis에서 RT 조회
        String savedToken = redisUtil.getRefreshToken(userId);

        if (savedToken == null) {
            throw new CustomException(UserErrorCode.LOGIN_REQUIRED);
        }

        // 3. RT 일치 확인 (불일치 = 탈취 감지 → 세션 전체 삭제)
        if (!savedToken.equals(refreshToken)) {
            redisUtil.deleteRefreshToken(userId);
            throw new CustomException(UserErrorCode.INVALID_TOKEN);
        }

        // 4. 최신 유저 정보 조회
        User user = userService.findActiveUserById(UUID.fromString(userId));

        // 5. 새 AT + RT 발급
        String newAccessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 6. 기존 RT 폐기 + 새 RT 저장 (RTR)
        redisUtil.saveRefreshToken(userId, newRefreshToken);

        return LogInResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
