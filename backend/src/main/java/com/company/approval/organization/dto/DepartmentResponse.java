package com.company.approval.organization.dto;

import java.util.ArrayList;
import java.util.List;

import com.company.approval.organization.domain.OrgDepartment;

public class DepartmentResponse {

    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private Long leaderUserId;
    private Integer sortOrder;
    private Boolean enabled;
    private List<DepartmentResponse> children = new ArrayList<DepartmentResponse>();

    public DepartmentResponse(OrgDepartment department) {
        this.id = department.getId();
        this.code = department.getCode();
        this.name = department.getName();
        this.parentId = department.getParentId();
        this.leaderUserId = department.getLeaderUserId();
        this.sortOrder = department.getSortOrder();
        this.enabled = department.getEnabled();
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

    public Long getParentId() {
        return parentId;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public List<DepartmentResponse> getChildren() {
        return children;
    }
}
