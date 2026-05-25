package com.company.approval.security.permission;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DataPermissionDecision {

    private final Long userId;
    private final Long departmentId;
    private final DataPermissionScope scope;
    private final Set<String> allowedApprovalTypes;

    public DataPermissionDecision(Long userId, Long departmentId, DataPermissionScope scope) {
        this(userId, departmentId, scope, Collections.<String>emptySet());
    }

    public DataPermissionDecision(Long userId, Long departmentId, DataPermissionScope scope, Set<String> allowedApprovalTypes) {
        this.userId = userId;
        this.departmentId = departmentId;
        this.scope = scope;
        this.allowedApprovalTypes = allowedApprovalTypes == null
                ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<String>(allowedApprovalTypes));
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

    public Set<String> getAllowedApprovalTypes() {
        return allowedApprovalTypes;
    }

    public boolean hasApprovalTypeRestriction() {
        return allowedApprovalTypes != null && !allowedApprovalTypes.isEmpty();
    }
}
