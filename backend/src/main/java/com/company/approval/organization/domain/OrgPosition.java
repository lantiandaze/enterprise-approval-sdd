package com.company.approval.organization.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "org_position")
public class OrgPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

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

    protected OrgPosition() {
    }

    public OrgPosition(Long departmentId, String code, String name) {
        OffsetDateTime now = OffsetDateTime.now();
        this.departmentId = departmentId;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
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

    public void update(Long departmentId, String code, String name, Integer sortOrder) {
        this.departmentId = departmentId;
        this.code = code;
        this.name = name;
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
