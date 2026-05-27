package com.msa.user_service.repository;

import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserRole;
import com.msa.user_service.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    Page<User> findAllByStatusAndDeletedAtIsNull(UserStatus status, Pageable pageable);

    List<User> findAllByUserIdInAndDeletedAtIsNull(Collection<UUID> userIds);

    List<User> findAllByHubIdAndRoleAndDeletedAtIsNull(UUID hubId, UserRole role);
}
