package com.msa.core_common.JpaAuditing.baseEntity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseEntity {
    @CreationTimestamp
    private LocalDateTime createdTime;
    @CreatedBy
    private String createdBy;
    @UpdateTimestamp
    private LocalDateTime updatedTime;
    @LastModifiedBy
    private String updatedBy;
    private LocalDateTime deletedTime;
    private String deletedBy;

    public void delete(String deletedBy) {
        this.deletedTime = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
