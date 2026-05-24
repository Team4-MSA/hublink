package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.dto.*;
import com.msa.user_service.entity.*;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.UserApprovalHistoryRepository;
import com.msa.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserApprovalHistoryRepository approvalHistoryRepository;
    private final HubManagerService hubManagerService;
    private final CompanyManagerService companyManagerService;
    private final DeliveryManagerService deliveryManagerService;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = findActiveUser(userId);
        user.update(request.getName(), request.getEmail(), request.getSlackId());
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(UUID userId, String deletedBy) {
        User user = findActiveUser(userId);
        user.delete(deletedBy);
    }

    public void approveUser(UUID userId, ApproveUserRequest request, UUID processedBy) {
        User user = findActiveUser(userId);
        if (user.getStatus() != UserStatus.PENDING) {
            throw new CustomException(UserErrorCode.NOT_PENDING_STATUS);
        }

        if (request.getStatus() == UserStatus.APPROVED) {
            switch (user.getRole()) {
                case HUB_MANAGER -> hubManagerService.validateHubExists(user.getHubId());
                case COMPANY_MANAGER -> companyManagerService.validateCompanyExists(user.getCompanyId());
                case DELIVERY_MANAGER -> {
                    if (request.getDeliveryManagerType() == null) {
                        throw new CustomException(UserErrorCode.DELIVERY_TYPE_REQUIRED);
                    }
                    deliveryManagerService.validateHubExists(user.getHubId());
                }
            }
        }

        executeApprovalTransaction(user, request, processedBy);
    }

    @Transactional
    public void executeApprovalTransaction(User user, ApproveUserRequest request, UUID processedBy) {
        UserStatus previousStatus = user.getStatus();

        if (request.getStatus() == UserStatus.APPROVED) {
            user.approve();

            switch (user.getRole()) {
                case HUB_MANAGER -> hubManagerService.createOnApproval(user.getUserId(), user.getHubId());
                case COMPANY_MANAGER -> companyManagerService.createOnApproval(user.getUserId(), user.getCompanyId());
                case DELIVERY_MANAGER -> deliveryManagerService.createOnApproval(user.getUserId(), user.getHubId(),
                            request.getDeliveryManagerType(), user.getSlackId());
            }
        } else if (request.getStatus() == UserStatus.REJECTED) {
            user.reject();
        } else if (request.getStatus() == UserStatus.INACTIVE) {
            user.inactive();
        }

        approvalHistoryRepository.save(UserApprovalHistory.builder()
                .userId(user.getUserId())
                .previousStatus(previousStatus)
                .newStatus(request.getStatus())
                .reason(request.getReason())
                .processedBy(processedBy)
                .build());
    }

    // Internal API용 - 허브 소속 여부 검증
    public boolean verifyHub(UUID userId, UUID hubId) {
        return hubManagerService.existsByUserIdAndHubId(userId, hubId);
    }

    // Internal API용 - 업체 소속 여부 검증
    public boolean verifyCompany(UUID userId, UUID companyId) {
        return companyManagerService.existsByUserIdAndCompanyId(userId, companyId);
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

    private User findActiveUser(UUID userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }
}
