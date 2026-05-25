package com.company.approval.security.permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataPermissionService {

    private static final Set<String> FINANCE_TYPES = unmodifiable(Arrays.asList("expense"));
    private static final Set<String> HR_TYPES = unmodifiable(Arrays.asList("leave", "overtime"));

    private final SysUserRepository userRepository;

    public DataPermissionService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DataPermissionDecision decide(UserPrincipal principal) {
        Long departmentId = findDepartmentId(principal.getUserId());
        if (principal.getRoles().contains("admin") || principal.getRoles().contains("general_manager")) {
            return new DataPermissionDecision(principal.getUserId(), departmentId, DataPermissionScope.ALL);
        }
        if (principal.getRoles().contains("finance")) {
            return new DataPermissionDecision(principal.getUserId(), departmentId, DataPermissionScope.ALL, FINANCE_TYPES);
        }
        if (principal.getRoles().contains("hr")) {
            return new DataPermissionDecision(principal.getUserId(), departmentId, DataPermissionScope.ALL, HR_TYPES);
        }
        if (principal.getRoles().contains("department_manager")) {
            return new DataPermissionDecision(principal.getUserId(), departmentId, DataPermissionScope.DEPARTMENT);
        }
        return new DataPermissionDecision(principal.getUserId(), departmentId, DataPermissionScope.SELF);
    }

    public boolean canAccessUser(UserPrincipal principal, Long targetUserId, Long targetDepartmentId) {
        DataPermissionDecision decision = decide(principal);
        if (decision.getScope() == DataPermissionScope.ALL && !decision.hasApprovalTypeRestriction()) {
            return true;
        }
        if (decision.getScope() == DataPermissionScope.DEPARTMENT) {
            return decision.getDepartmentId() != null && decision.getDepartmentId().equals(targetDepartmentId);
        }
        return principal.getUserId().equals(targetUserId);
    }

    private Long findDepartmentId(Long userId) {
        SysUser user = userRepository.findById(userId).orElse(null);
        return user == null || Boolean.TRUE.equals(user.getDeleted()) ? null : user.getDepartmentId();
    }

    private static Set<String> unmodifiable(java.util.List<String> values) {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(values));
    }
}
