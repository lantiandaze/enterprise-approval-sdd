package com.company.approval.system.dto;

import java.util.List;

public class RolePermissionResponse {

    private Long roleId;
    private List<Long> permissionIds;

    public RolePermissionResponse(Long roleId, List<Long> permissionIds) {
        this.roleId = roleId;
        this.permissionIds = permissionIds;
    }

    public Long getRoleId() {
        return roleId;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }
}
