package com.company.approval.organization.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "org_department")
public class OrgDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String code;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "leader_user_id")
    private Long leaderUserId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean enabled;

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

    protected OrgDepartment() {
    }

    public OrgDepartment(String code, String name) {
        OffsetDateTime now = OffsetDateTime.now();
        this.code = code;
        this.name = name;
        this.sortOrder = 0;
        this.enabled = true;
        this.deleted = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void update(String code, String name, Long parentId, Long leaderUserId, Integer sortOrder) {
        this.code = code;
        this.name = name;
        this.parentId = parentId;
        this.leaderUserId = leaderUserId;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = OffsetDateTime.now();
    }

    public void softDelete(Long operatorId) {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = operatorId;
        this.updatedAt = OffsetDateTime.now();
    }
}
