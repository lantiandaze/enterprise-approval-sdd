package com.company.approval.system.dto;

import com.company.approval.system.domain.SysRole;

public class RoleResponse {

    private Long id;
    private String code;
    private String name;
    private Boolean enabled;

    public RoleResponse(SysRole role) {
        this.id = role.getId();
        this.code = role.getCode();
        this.name = role.getName();
        this.enabled = role.getEnabled();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
