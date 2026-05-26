package com.msa.hub_service.global;

import com.msa.core_common.auth.UserRole;
import com.msa.core_common.error.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class RoleCheckAspect {
    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new CustomException(HubErrorCode.INTERNAL_SERVER_ERROR);
        }

        HttpServletRequest request = attributes.getRequest();

        // 헤더에서 ID와 권한 가져오기
        String userId = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        if (!StringUtils.hasText(userId) || !StringUtils.hasText(roleHeader)) {
            log.warn("인증 정보가 없습니다. 헤더 확인 필요");
            throw new CustomException(HubErrorCode.UNAUTHORIZED);
        }

        try {
            UserRole userRole = UserRole.valueOf(roleHeader);

            boolean hasAccess = Arrays.asList(requireRole.value()).contains(userRole);

            if (!hasAccess) {
                log.warn("권한이 부족합니다. User ID: {}, Role: {}", userId, userRole);
                throw new CustomException(HubErrorCode.FORBIDDEN);
            }

        } catch (IllegalArgumentException e) {
            throw new CustomException(HubErrorCode.UNAUTHORIZED);
        }
    }
}
