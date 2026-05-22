package com.company.approval.system.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UserRequest {

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String displayName;

    @Size(max = 64)
    private String employeeNo;

    @Size(max = 128)
    private String email;

    @Size(max = 32)
    private String phone;

    private String password;
    private Long departmentId;
    private Long positionId;
    private Long directManagerId;
    private List<Long> roleIds;

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

    public String getPassword() {
        return password;
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

    public List<Long> getRoleIds() {
        return roleIds;
    }
}
