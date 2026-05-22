package com.company.approval.security.permission;

public class DataPermissionDecision {

    private final Long userId;
    private final Long departmentId;
    private final DataPermissionScope scope;

    public DataPermissionDecision(Long userId, Long departmentId, DataPermissionScope scope) {
        this.userId = userId;
        this.departmentId = departmentId;
        this.scope = scope;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public DataPermissionScope getScope() {
        return scope;
    }
}
