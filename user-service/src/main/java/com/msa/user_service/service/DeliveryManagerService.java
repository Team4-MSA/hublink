package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.HubClient;
import com.msa.user_service.dto.DeliveryManagerRequest;
import com.msa.user_service.dto.DeliveryManagerResponse;
import com.msa.user_service.dto.InternalDeliveryManagerResponse;
import com.msa.user_service.entity.DeliveryManager;
import com.msa.user_service.entity.DeliveryManagerType;
import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserRole;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.DeliveryManagerRepository;
import com.msa.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final UserRepository userRepository;
    private final HubClient hubClient;

    public void validateHubExists(UUID hubId) {
        if (!hubClient.checkHubExists(hubId).isExists()) {
            throw new CustomException(UserErrorCode.HUB_NOT_FOUND);
        }
    }

    private void validateHubAccess(UUID requestUserId, UUID hubId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(requestUserId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        if (user.getRole() != UserRole.HUB_MANAGER || !hubId.equals(user.getHubId())) {
            throw new CustomException(UserErrorCode.HUB_ACCESS_DENIED);
        }
    }

    private UUID getMyHubId(UUID requestUserId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(requestUserId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        if (user.getHubId() == null) {
            throw new CustomException(UserErrorCode.NO_ASSIGNED_HUB);
        }
        return user.getHubId();
    }

    @Transactional
    public DeliveryManagerResponse register(DeliveryManagerRequest request, String role, UUID requestUserId) {
        if (role.equals("HUB_MANAGER")) {
            validateHubAccess(requestUserId, request.getHubId());
        }

        validateHubExists(request.getHubId());

        User user = userRepository.findByUserIdAndDeletedAtIsNull(request.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        DeliveryManager deliveryManager = saveDeliveryManager(request.getUserId(), request.getHubId(), request.getType(), user.getSlackId());

        return DeliveryManagerResponse.from(deliveryManager);
    }

    public PageRes<DeliveryManagerResponse> getList(UUID hubId, DeliveryManagerType type, Pageable pageable,
                                                     String role, UUID requestUserId) {
        if (role.equals("HUB_MANAGER")) {
            UUID myHubId = getMyHubId(requestUserId);

            if (hubId != null) {
                if (!myHubId.equals(hubId)) {
                    throw new CustomException(UserErrorCode.HUB_ACCESS_DENIED);
                }
                return type != null
                        ? new PageRes<>(deliveryManagerRepository.findAllByHubIdAndTypeAndDeletedAtIsNull(hubId, type, pageable).map(DeliveryManagerResponse::from))
                        : new PageRes<>(deliveryManagerRepository.findAllByHubIdAndDeletedAtIsNull(hubId, pageable).map(DeliveryManagerResponse::from));
            }

            return type != null
                    ? new PageRes<>(deliveryManagerRepository.findAllByHubIdAndTypeAndDeletedAtIsNull(myHubId, type, pageable).map(DeliveryManagerResponse::from))
                    : new PageRes<>(deliveryManagerRepository.findAllByHubIdAndDeletedAtIsNull(myHubId, pageable).map(DeliveryManagerResponse::from));
        }

        if (hubId != null && type != null) {
            return new PageRes<>(deliveryManagerRepository.findAllByHubIdAndTypeAndDeletedAtIsNull(hubId, type, pageable).map(DeliveryManagerResponse::from));
        }
        if (hubId != null) {
            return new PageRes<>(deliveryManagerRepository.findAllByHubIdAndDeletedAtIsNull(hubId, pageable).map(DeliveryManagerResponse::from));
        }
        if (type != null) {
            return new PageRes<>(deliveryManagerRepository.findAllByTypeAndDeletedAtIsNull(type, pageable).map(DeliveryManagerResponse::from));
        }
        return new PageRes<>(deliveryManagerRepository.findAllByDeletedAtIsNull(pageable).map(DeliveryManagerResponse::from));
    }

    public DeliveryManagerResponse getOne(UUID userId, String role, UUID requestUserId) {
        DeliveryManager deliveryManager = deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (role.equals("HUB_MANAGER")) {
            validateHubAccess(requestUserId, deliveryManager.getHubId());
        }
        if (role.equals("DELIVERY_MANAGER")) {
            if (!deliveryManager.getUserId().equals(requestUserId)) {
                throw new CustomException(UserErrorCode.SELF_ONLY_ACCESS);
            }
        }

        return DeliveryManagerResponse.from(deliveryManager);
    }

    public List<InternalDeliveryManagerResponse> getDeliveryManagersByHubsForInternal(List<UUID> hubIds) {
        List<DeliveryManager> deliveryManagers = deliveryManagerRepository.findAllByHubIdInAndDeletedAtIsNull(hubIds);

        List<UUID> userIds = deliveryManagers.stream()
                .map(DeliveryManager::getUserId)
                .toList();

        Map<UUID, User> userMap = userRepository.findAllByUserIdInAndDeletedAtIsNull(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        return deliveryManagers.stream()
                .map(dm -> {
                    User user = userMap.get(dm.getUserId());
                    if (user == null) {
                        throw new CustomException(UserErrorCode.USER_NOT_FOUND);
                    }
                    return InternalDeliveryManagerResponse.of(dm, user);
                })
                .toList();
    }

    @Transactional
    public void delete(UUID userId, String deletedBy, String role, UUID requestUserId) {
        DeliveryManager deliveryManager = deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (role.equals("HUB_MANAGER")) {
            validateHubAccess(requestUserId, deliveryManager.getHubId());
        }

        deliveryManager.delete(deletedBy);
    }

    // 승인 흐름 전용
    @Transactional
    public void createOnApproval(UUID userId, UUID hubId, DeliveryManagerType type, String slackId) {
        saveDeliveryManager(userId, hubId, type, slackId);
    }

    @Transactional
    public void updateSlackId(UUID userId, String slackId) {
        deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(userId)
                .ifPresent(dm -> dm.updateSlackId(slackId));
    }

    private DeliveryManager saveDeliveryManager(UUID userId, UUID hubId, DeliveryManagerType type, String slackId) {
        int nextSequence = deliveryManagerRepository
                .findLatestByHubId(hubId)
                .map(dm -> dm.getDeliverySequence() + 1)
                .orElse(1);

        return deliveryManagerRepository.save(DeliveryManager.builder()
                .userId(userId)
                .hubId(hubId)
                .type(type)
                .deliverySequence(nextSequence)
                .slackId(slackId)
                .build());
    }
}
