package com.company.approval.system.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_user_role")
public class SysUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SysUserRole() {
    }

    public SysUserRole(Long userId, Long roleId) {
        OffsetDateTime now = OffsetDateTime.now();
        this.userId = userId;
        this.roleId = roleId;
        this.deleted = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void softDelete(Long operatorId) {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = operatorId;
        this.updatedAt = OffsetDateTime.now();
    }
}
