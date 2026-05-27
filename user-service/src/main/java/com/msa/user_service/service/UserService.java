package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.CompanyClient;
import com.msa.user_service.client.HubClient;
import com.msa.user_service.dto.*;
import com.msa.user_service.entity.*;
import java.util.List;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.UserRepository;
import com.msa.user_service.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserApprovalService userApprovalService;
    private final DeliveryManagerService deliveryManagerService;
    private final HubClient hubClient;
    private final CompanyClient companyClient;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(UserErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .slackId(request.getSlackId())
                .role(request.getRole())
                .status(UserStatus.PENDING)
                .hubId(request.getHubId())
                .companyId(request.getCompanyId())
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getUser(UUID userId) {
        return UserResponse.from(findActiveUser(userId));
    }

    public PageRes<UserResponse> getUsers(Pageable pageable) {
        return new PageRes<>(userRepository.findAllByDeletedAtIsNull(pageable)
                .map(UserResponse::from));
    }

    public PageRes<UserResponse> getPendingUsers(Pageable pageable) {
        return new PageRes<>(userRepository.findAllByStatusAndDeletedAtIsNull(UserStatus.PENDING, pageable)
                .map(UserResponse::from));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateUpdateResources(UpdateUserRequest request) {
        if (request.getHubId() != null && !hubClient.checkHubExists(request.getHubId()).isExists()) {
            throw new CustomException(UserErrorCode.HUB_NOT_FOUND);
        }
        if (request.getCompanyId() != null && !companyClient.checkCompanyExists(request.getCompanyId()).isExists()) {
            throw new CustomException(UserErrorCode.COMPANY_NOT_FOUND);
        }
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = findActiveUser(userId);
        UUID oldHubId = user.getHubId();
        user.update(request.getName(), request.getEmail(), request.getSlackId(),
                request.getHubId(), request.getCompanyId());

        if (user.getRole() == UserRole.DELIVERY_MANAGER) {
            if (request.getSlackId() != null) {
                deliveryManagerService.updateSlackId(userId, request.getSlackId());
            }
            if (request.getHubId() != null && !request.getHubId().equals(oldHubId)) {
                deliveryManagerService.syncHubId(userId, request.getHubId());
            }
        }

        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(UUID userId, String deletedBy) {
        User user = findActiveUser(userId);
        user.delete(deletedBy);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisUtil.invalidateUser(userId.toString(), accessExpiration);
            }
        });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void approveUser(UUID userId, ApproveUserRequest request, UUID processedBy) {
        User user = findActiveUser(userId);
        if (user.getStatus() != UserStatus.PENDING) {
            throw new CustomException(UserErrorCode.NOT_PENDING_STATUS);
        }

        if (request.getStatus() == UserStatus.APPROVED) {
            switch (user.getRole()) {
                case HUB_MANAGER -> {
                    if (user.getHubId() == null || !hubClient.checkHubExists(user.getHubId()).isExists()) {
                        throw new CustomException(UserErrorCode.HUB_NOT_FOUND);
                    }
                }
                case COMPANY_MANAGER -> {
                    if (user.getCompanyId() == null || !companyClient.checkCompanyExists(user.getCompanyId()).isExists()) {
                        throw new CustomException(UserErrorCode.COMPANY_NOT_FOUND);
                    }
                }
                case DELIVERY_MANAGER -> {
                    if (request.getDeliveryManagerType() == null) {
                        throw new CustomException(UserErrorCode.DELIVERY_TYPE_REQUIRED);
                    }
                    if (user.getHubId() == null || !hubClient.checkHubExists(user.getHubId()).isExists()) {
                        throw new CustomException(UserErrorCode.HUB_NOT_FOUND);
                    }
                }
            }
        }

        userApprovalService.executeApproval(userId, request, processedBy);
    }

    // Internal API용 - 허브 담당 HUB_MANAGER 목록 조회
    public List<InternalHubManagerResponse> getHubManagersByHubId(UUID hubId) {
        return userRepository.findAllByHubIdAndRoleAndDeletedAtIsNull(hubId, UserRole.HUB_MANAGER)
                .stream()
                .map(InternalHubManagerResponse::of)
                .toList();
    }

    // Internal API용 - 허브 소속 여부 검증
    public boolean verifyHub(UUID userId, UUID hubId) {
        User user = findActiveUser(userId);
        return hubId.equals(user.getHubId());
    }

    // Internal API용 - 업체 소속 여부 검증
    public boolean verifyCompany(UUID userId, UUID companyId) {
        User user = findActiveUser(userId);
        return companyId.equals(user.getCompanyId());
    }

    // username으로 User 엔티티 조회
    public User findUserForAuth(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    // userId로 User 엔티티 조회
    public User findActiveUserById(UUID userId) {
        return findActiveUser(userId);
    }

    // Internal API용 - 다른 서비스에서 유저 정보 조회
    public UserAuthResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return UserAuthResponse.from(user);
    }

    // Internal API용 - 다른 서비스에서 유저 정보 조회
    public UserAuthResponse getUserById(UUID userId) {
        return UserAuthResponse.from(findActiveUser(userId));
    }

    // Internal API용 - 복수 userId로 이름 + 소속 업체 조회
    public List<UserNameAndCompanyResponse> getUserNamesAndCompanies(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllByUserIdInAndDeletedAtIsNull(userIds)
                .stream()
                .map(UserNameAndCompanyResponse::from)
                .toList();
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }
}
