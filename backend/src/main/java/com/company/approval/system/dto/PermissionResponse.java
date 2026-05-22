package com.company.approval.system.dto;

import com.company.approval.system.domain.SysPermission;

public class PermissionResponse {

    private Long id;
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private Integer sortOrder;
    private Boolean enabled;

    public PermissionResponse(SysPermission permission) {
        this.id = permission.getId();
        this.code = permission.getCode();
        this.name = permission.getName();
        this.type = permission.getType();
        this.parentId = permission.getParentId();
        this.sortOrder = permission.getSortOrder();
        this.enabled = permission.getEnabled();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public Long getParentId() { return parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public Boolean getEnabled() { return enabled; }
}
