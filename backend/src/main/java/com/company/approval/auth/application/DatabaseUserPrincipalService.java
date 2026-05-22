package com.company.approval.auth.application;

import java.util.Collections;
import java.util.List;

import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysRolePermissionRepository;
import com.company.approval.system.repository.SysUserRepository;
import com.company.approval.system.repository.SysUserRoleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("postgres")
public class DatabaseUserPrincipalService implements UserPrincipalService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;

    public DatabaseUserPrincipalService(
            SysUserRepository userRepository,
            SysUserRoleRepository userRoleRepository,
            SysRolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPrincipal loadByUsername(String username) {
        SysUser user = userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
        if (user == null || !"active".equals(user.getStatus())) {
            return null;
        }

        List<String> roles = userRoleRepository.findRoleCodesByUserId(user.getId());
        List<String> permissions = rolePermissionRepository.findPermissionCodesByUserId(user.getId());
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPasswordHash(),
                Collections.unmodifiableList(roles),
                Collections.unmodifiableList(permissions));
    }
}
