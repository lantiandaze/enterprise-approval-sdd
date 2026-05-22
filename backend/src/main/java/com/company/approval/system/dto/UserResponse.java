package com.company.approval.system.dto;

import java.util.List;

import com.company.approval.system.domain.SysUser;

public class UserResponse {

    private Long id;
    private String username;
    private String displayName;
    private String employeeNo;
    private String email;
    private String phone;
    private String status;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionName;
    private Long directManagerId;
    private String directManagerName;
    private List<RoleResponse> roles;

    public UserResponse(
            SysUser user,
            String departmentName,
            String positionName,
            String directManagerName,
            List<RoleResponse> roles) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.employeeNo = user.getEmployeeNo();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.status = user.getStatus();
        this.departmentId = user.getDepartmentId();
        this.departmentName = departmentName;
        this.positionId = user.getPositionId();
        this.positionName = positionName;
        this.directManagerId = user.getDirectManagerId();
        this.directManagerName = directManagerName;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
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

    public String getStatus() {
        return status;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Long getPositionId() {
        return positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public Long getDirectManagerId() {
        return directManagerId;
    }

    public String getDirectManagerName() {
        return directManagerName;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }
}
