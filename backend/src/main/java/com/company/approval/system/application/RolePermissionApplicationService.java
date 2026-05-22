package com.company.approval.system.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.system.domain.SysPermission;
import com.company.approval.system.domain.SysRole;
import com.company.approval.system.domain.SysRolePermission;
import com.company.approval.system.dto.PermissionResponse;
import com.company.approval.system.dto.RolePermissionResponse;
import com.company.approval.system.dto.RoleResponse;
import com.company.approval.system.repository.SysPermissionRepository;
import com.company.approval.system.repository.SysRolePermissionRepository;
import com.company.approval.system.repository.SysRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolePermissionApplicationService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;

    public RolePermissionApplicationService(
            SysRoleRepository roleRepository,
            SysPermissionRepository permissionRepository,
            SysRolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        List<RoleResponse> responses = new ArrayList<RoleResponse>();
        for (SysRole role : roleRepository.findByDeletedFalseOrderByIdAsc()) {
            responses.add(new RoleResponse(role));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        List<PermissionResponse> responses = new ArrayList<PermissionResponse>();
        for (SysPermission permission : permissionRepository.findByDeletedFalseOrderBySortOrderAscIdAsc()) {
            responses.add(new PermissionResponse(permission));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public RolePermissionResponse getRolePermissions(Long roleId) {
        findRole(roleId);
        return new RolePermissionResponse(roleId, rolePermissionRepository.findPermissionIdsByRoleId(roleId));
    }

    @Transactional
    public RolePermissionResponse saveRolePermissions(Long roleId, List<Long> permissionIds, Long operatorId) {
        findRole(roleId);
        List<Long> targetIds = permissionIds == null ? new ArrayList<Long>() : permissionIds;
        for (Long permissionId : targetIds) {
            findPermission(permissionId);
        }

        Map<Long, SysRolePermission> currentByPermissionId = new LinkedHashMap<Long, SysRolePermission>();
        for (SysRolePermission relation : rolePermissionRepository.findByRoleIdAndDeletedFalse(roleId)) {
            currentByPermissionId.put(relation.getPermissionId(), relation);
        }

        for (Long permissionId : targetIds) {
            if (!currentByPermissionId.containsKey(permissionId)) {
                rolePermissionRepository.save(new SysRolePermission(roleId, permissionId));
            }
        }
        for (SysRolePermission relation : currentByPermissionId.values()) {
            if (!targetIds.contains(relation.getPermissionId())) {
                relation.softDelete(operatorId);
            }
        }
        return getRolePermissions(roleId);
    }

    private SysRole findRole(Long roleId) {
        SysRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Role not found"));
        if (!Boolean.TRUE.equals(role.getEnabled())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Role disabled");
        }
        return role;
    }

    private SysPermission findPermission(Long permissionId) {
        SysPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Permission not found"));
        if (!Boolean.TRUE.equals(permission.getEnabled())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Permission disabled");
        }
        return permission;
    }
}
