package com.company.approval.organization.dto;

import com.company.approval.organization.domain.OrgPosition;

public class PositionResponse {

    private Long id;
    private Long departmentId;
    private String code;
    private String name;
    private Integer sortOrder;
    private Boolean enabled;

    public PositionResponse(OrgPosition position) {
        this.id = position.getId();
        this.departmentId = position.getDepartmentId();
        this.code = position.getCode();
        this.name = position.getName();
        this.sortOrder = position.getSortOrder();
        this.enabled = position.getEnabled();
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
}
