package org.tracker.ubus.ubus.Components.Shared.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

/**
 * all jpa entities that have createdAt and updatedAt fields should extend this class to avoid code duplication
 * author: Hlahla K
 */
@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class TimeAuditableEntity {

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onSave() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
