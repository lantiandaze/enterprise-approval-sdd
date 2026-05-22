package com.company.approval.security.permission;

import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataPermissionService {

    private final SysUserRepository userRepository;

    public DataPermissionService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DataPermissionDecision decide(UserPrincipal principal) {
        if (principal.getRoles().contains("admin")
                || principal.getRoles().contains("general_manager")
                || principal.getRoles().contains("finance")
                || principal.getRoles().contains("hr")) {
            return new DataPermissionDecision(principal.getUserId(), findDepartmentId(principal.getUserId()), DataPermissionScope.ALL);
        }
        if (principal.getRoles().contains("department_manager")) {
            return new DataPermissionDecision(principal.getUserId(), findDepartmentId(principal.getUserId()), DataPermissionScope.DEPARTMENT);
        }
        return new DataPermissionDecision(principal.getUserId(), findDepartmentId(principal.getUserId()), DataPermissionScope.SELF);
    }

    public boolean canAccessUser(UserPrincipal principal, Long targetUserId, Long targetDepartmentId) {
        DataPermissionDecision decision = decide(principal);
        if (decision.getScope() == DataPermissionScope.ALL) {
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
}
