package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.HubClient;
import com.msa.user_service.dto.HubManagerRequest;
import com.msa.user_service.dto.HubManagerResponse;
import com.msa.user_service.dto.InternalHubManagerResponse;
import com.msa.user_service.entity.HubManager;
import com.msa.user_service.entity.User;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.HubManagerRepository;
import com.msa.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubManagerService {

    private final HubManagerRepository hubManagerRepository;
    private final UserRepository userRepository;
    private final HubClient hubClient;

    public void validateHubExists(UUID hubId) {
        if (!hubClient.checkHubExists(hubId).isExists()) {
            throw new CustomException(UserErrorCode.HUB_NOT_FOUND);
        }
    }

    @Transactional
    public HubManagerResponse register(HubManagerRequest request) {
        validateHubExists(request.getHubId());

        HubManager hubManager = saveHubManager(request.getUserId(), request.getHubId());
        return HubManagerResponse.from(hubManager);
    }

    // 승인 흐름 전용
    @Transactional
    public void createOnApproval(UUID userId, UUID hubId) {
        saveHubManager(userId, hubId);
    }

    private HubManager saveHubManager(UUID userId, UUID hubId) {
        return hubManagerRepository.save(HubManager.builder()
                .userId(userId)
                .hubId(hubId)
                .build());
    }

    // UserService Internal API용 - 허브 소속 여부 확인
    public boolean existsByUserIdAndHubId(UUID userId, UUID hubId) {
        return hubManagerRepository.existsByUserIdAndHubIdAndDeletedAtIsNull(userId, hubId);
    }

    public PageRes<HubManagerResponse> getList(UUID hubId, Pageable pageable) {
        if (hubId != null) {
            return new PageRes<>(hubManagerRepository.findAllByHubIdAndDeletedAtIsNull(hubId, pageable)
                    .map(HubManagerResponse::from));
        }
        return new PageRes<>(hubManagerRepository.findAllByDeletedAtIsNull(pageable)
                .map(HubManagerResponse::from));
    }

    public HubManagerResponse getOne(UUID hubManagerId) {
        HubManager hubManager = hubManagerRepository.findByHubManagerIdAndDeletedAtIsNull(hubManagerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.HUB_MANAGER_NOT_FOUND));
        return HubManagerResponse.from(hubManager);
    }

    public InternalHubManagerResponse getHubManagerByHubIdForInternal(UUID hubId) {
        HubManager hubManager = hubManagerRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new CustomException(UserErrorCode.HUB_MANAGER_NOT_FOUND));
        User user = userRepository.findByUserIdAndDeletedAtIsNull(hubManager.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return InternalHubManagerResponse.of(hubManager, user);
    }

    @Transactional
    public void delete(UUID hubManagerId, String deletedBy) {
        HubManager hubManager = hubManagerRepository.findByHubManagerIdAndDeletedAtIsNull(hubManagerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.HUB_MANAGER_NOT_FOUND));
        hubManager.delete(deletedBy);
    }
}
