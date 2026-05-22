package com.company.approval.system.domain;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "employee_no", length = 64)
    private String employeeNo;

    @Column(length = 128)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "direct_manager_id")
    private Long directManagerId;

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

    protected SysUser() {
    }

    public SysUser(String username, String passwordHash, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.status = "active";
        this.deleted = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public Long getDirectManagerId() {
        return directManagerId;
    }

    public void updateProfile(
            String username,
            String displayName,
            String employeeNo,
            String email,
            String phone,
            Long departmentId,
            Long positionId,
            Long directManagerId) {
        this.username = username;
        this.displayName = displayName;
        this.employeeNo = employeeNo;
        this.email = email;
        this.phone = phone;
        this.departmentId = departmentId;
        this.positionId = positionId;
        this.directManagerId = directManagerId;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public void softDelete(Long operatorId) {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now();
        this.deletedBy = operatorId;
        this.updatedAt = OffsetDateTime.now();
    }
}
