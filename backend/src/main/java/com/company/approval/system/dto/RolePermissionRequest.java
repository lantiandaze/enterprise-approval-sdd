package com.company.approval.system.dto;

import java.util.List;
import javax.validation.constraints.NotNull;

public class RolePermissionRequest {

    @NotNull
    private List<Long> permissionIds;

    public List<Long> getPermissionIds() {
        return permissionIds;
    }
}
