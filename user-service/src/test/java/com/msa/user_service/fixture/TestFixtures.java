package com.msa.user_service.fixture;

import com.msa.user_service.dto.*;
import com.msa.user_service.entity.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestFixtures {

    //  고정 UUID
    public static final UUID USER_ID            = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    public static final UUID ADMIN_ID           = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000099");
    public static final UUID HUB_ID             = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001");
    public static final UUID COMPANY_ID         = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
    public static final UUID HUB_MANAGER_ID     = UUID.fromString("dddddddd-0000-0000-0000-000000000001");
    public static final UUID COMPANY_MANAGER_ID = UUID.fromString("eeeeeeee-0000-0000-0000-000000000001");
    public static final UUID DELIVERY_MANAGER_ID= UUID.fromString("ffffffff-0000-0000-0000-000000000001");

    // User

    /** 승인된 MASTER 유저 */
    public static User approvedMasterUser() {
        User user = User.builder()
                .username("masteruser")
                .password("$2a$10$encodedpasswordhash")
                .name("마스터유저")
                .email("master@example.com")
                .slackId("U_MASTER")
                .role(UserRole.MASTER)
                .status(UserStatus.APPROVED)
                .build();
        setField(user, "userId", USER_ID);
        setAuditFields(user);
        return user;
    }

    /** 승인 대기중인 HUB_MANAGER 유저 */
    public static User pendingHubManagerUser() {
        User user = User.builder()
                .username("hubmanager1")
                .password("$2a$10$encodedpasswordhash")
                .name("허브매니저")
                .email("hubmgr@example.com")
                .slackId("U_HUB")
                .role(UserRole.HUB_MANAGER)
                .status(UserStatus.PENDING)
                .hubId(HUB_ID)
                .build();
        setField(user, "userId", USER_ID);
        setAuditFields(user);
        return user;
    }

    /** 승인 대기중인 COMPANY_MANAGER 유저 */
    public static User pendingCompanyManagerUser() {
        User user = User.builder()
                .username("compmgr1")
                .password("$2a$10$encodedpasswordhash")
                .name("업체매니저")
                .email("compmgr@example.com")
                .slackId("U_COMP")
                .role(UserRole.COMPANY_MANAGER)
                .status(UserStatus.PENDING)
                .companyId(COMPANY_ID)
                .build();
        setField(user, "userId", USER_ID);
        setAuditFields(user);
        return user;
    }

    /** 승인 대기중인 DELIVERY_MANAGER 유저 */
    public static User pendingDeliveryManagerUser() {
        User user = User.builder()
                .username("delivmgr1")
                .password("$2a$10$encodedpasswordhash")
                .name("배송매니저")
                .email("deliv@example.com")
                .slackId("U_DELIV")
                .role(UserRole.DELIVERY_MANAGER)
                .status(UserStatus.PENDING)
                .hubId(HUB_ID)
                .build();
        setField(user, "userId", USER_ID);
        setAuditFields(user);
        return user;
    }

    // HubManager

    public static HubManager hubManager() {
        HubManager hm = HubManager.builder()
                .userId(USER_ID)
                .hubId(HUB_ID)
                .build();
        setField(hm, "hubManagerId", HUB_MANAGER_ID);
        setAuditFields(hm);
        return hm;
    }

    // CompanyManager

    public static CompanyManager companyManager() {
        CompanyManager cm = CompanyManager.builder()
                .userId(USER_ID)
                .companyId(COMPANY_ID)
                .build();
        setField(cm, "companyManagerId", COMPANY_MANAGER_ID);
        setAuditFields(cm);
        return cm;
    }

    // DeliveryManager

    public static DeliveryManager hubDeliveryManager() {
        DeliveryManager dm = DeliveryManager.builder()
                .userId(USER_ID)
                .hubId(HUB_ID)
                .type(DeliveryManagerType.HUB_DELIVERY)
                .deliverySequence(1)
                .slackId("U_DELIV")
                .build();
        setField(dm, "deliveryManagerId", DELIVERY_MANAGER_ID);
        setAuditFields(dm);
        return dm;
    }

    // Response DTO

    public static UserResponse userResponse() {
        return UserResponse.from(approvedMasterUser());
    }

    public static HubManagerResponse hubManagerResponse() {
        return HubManagerResponse.from(hubManager());
    }

    public static CompanyManagerResponse companyManagerResponse() {
        return CompanyManagerResponse.from(companyManager());
    }

    public static DeliveryManagerResponse deliveryManagerResponse() {
        return DeliveryManagerResponse.from(hubDeliveryManager());
    }

    // 내부 유틸

    public static void setField(Object target, String field, Object value) {
        ReflectionTestUtils.setField(target, field, value);
    }

    private static void setAuditFields(Object target) {
        LocalDateTime now = LocalDateTime.now();
        try {
            ReflectionTestUtils.setField(target, "createdAt", now);
            ReflectionTestUtils.setField(target, "updatedAt", now);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
