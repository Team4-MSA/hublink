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
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.DeliveryManagerRepository;
import com.msa.user_service.repository.HubManagerRepository;
import com.msa.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final HubManagerRepository hubManagerRepository;
    private final UserRepository userRepository;
    private final HubClient hubClient;

    public void validateHubExists(UUID hubId) {
        if (!hubClient.checkHubExists(hubId).isExists()) {
            throw new CustomException(UserErrorCode.HUB_NOT_FOUND);
        }
    }

    private void validateHubAccess(UUID requestUserId, UUID hubId) {
        if (!hubManagerRepository.existsByUserIdAndHubIdAndDeletedAtIsNull(requestUserId, hubId)) {
            throw new CustomException(UserErrorCode.HUB_ACCESS_DENIED);
        }
    }

    private List<UUID> getMyHubIds(UUID requestUserId) {
        List<UUID> hubIds = hubManagerRepository.findAllByUserIdAndDeletedAtIsNull(requestUserId)
                .stream()
                .map(hm -> hm.getHubId())
                .toList();
        if (hubIds.isEmpty()) {
            throw new CustomException(UserErrorCode.NO_ASSIGNED_HUB);
        }
        return hubIds;
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
            List<UUID> myHubIds = getMyHubIds(requestUserId);

            if (hubId != null) {
                if (!myHubIds.contains(hubId)) {
                    throw new CustomException(UserErrorCode.HUB_ACCESS_DENIED);
                }
                return type != null
                        ? new PageRes<>(deliveryManagerRepository.findAllByHubIdAndTypeAndDeletedAtIsNull(hubId, type, pageable).map(DeliveryManagerResponse::from))
                        : new PageRes<>(deliveryManagerRepository.findAllByHubIdAndDeletedAtIsNull(hubId, pageable).map(DeliveryManagerResponse::from));
            }

            return type != null
                    ? new PageRes<>(deliveryManagerRepository.findAllByHubIdInAndTypeAndDeletedAtIsNull(myHubIds, type, pageable).map(DeliveryManagerResponse::from))
                    : new PageRes<>(deliveryManagerRepository.findAllByHubIdInAndDeletedAtIsNull(myHubIds, pageable).map(DeliveryManagerResponse::from));
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

    public DeliveryManagerResponse getOne(UUID deliveryManagerId, String role, UUID requestUserId) {
        DeliveryManager deliveryManager = deliveryManagerRepository.findByDeliveryManagerIdAndDeletedAtIsNull(deliveryManagerId)
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

    public List<InternalDeliveryManagerResponse> getDeliveryManagersByHubForInternal(UUID hubId) {
        List<DeliveryManager> deliveryManagers = deliveryManagerRepository.findAllByHubIdAndDeletedAtIsNull(hubId);
        return deliveryManagers.stream()
                .map(dm -> {
                    User user = userRepository.findByUserIdAndDeletedAtIsNull(dm.getUserId())
                            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
                    return InternalDeliveryManagerResponse.of(dm, user);
                })
                .toList();
    }

    @Transactional
    public void delete(UUID deliveryManagerId, String deletedBy, String role, UUID requestUserId) {
        DeliveryManager deliveryManager = deliveryManagerRepository.findByDeliveryManagerIdAndDeletedAtIsNull(deliveryManagerId)
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
