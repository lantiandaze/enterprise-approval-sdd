package com.company.approval.system.dto;

import java.util.List;
import javax.validation.constraints.NotNull;

public class UserRoleRequest {

    @NotNull
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }
}
