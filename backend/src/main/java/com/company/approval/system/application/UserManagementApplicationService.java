package com.company.approval.system.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.organization.domain.OrgDepartment;
import com.company.approval.organization.domain.OrgPosition;
import com.company.approval.organization.repository.OrgDepartmentRepository;
import com.company.approval.organization.repository.OrgPositionRepository;
import com.company.approval.system.domain.SysRole;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.domain.SysUserRole;
import com.company.approval.system.dto.RoleResponse;
import com.company.approval.system.dto.UserRequest;
import com.company.approval.system.dto.UserResponse;
import com.company.approval.system.repository.SysRoleRepository;
import com.company.approval.system.repository.SysUserRepository;
import com.company.approval.system.repository.SysUserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserManagementApplicationService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final OrgDepartmentRepository departmentRepository;
    private final OrgPositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementApplicationService(
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            SysUserRoleRepository userRoleRepository,
            OrgDepartmentRepository departmentRepository,
            OrgPositionRepository positionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        List<SysUser> users = userRepository.findByDeletedFalseOrderByIdAsc();
        List<UserResponse> responses = new ArrayList<UserResponse>();
        for (SysUser user : users) {
            responses.add(toResponse(user));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        List<RoleResponse> responses = new ArrayList<RoleResponse>();
        for (SysRole role : roleRepository.findByDeletedFalseOrderByIdAsc()) {
            responses.add(new RoleResponse(role));
        }
        return responses;
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Password is required");
        }
        ensureUsernameAvailable(request.getUsername(), null);
        validateOrganization(request);
        SysUser user = new SysUser(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getDisplayName());
        user.updateProfile(
                request.getUsername(),
                request.getDisplayName(),
                request.getEmployeeNo(),
                request.getEmail(),
                request.getPhone(),
                request.getDepartmentId(),
                request.getPositionId(),
                request.getDirectManagerId());
        SysUser saved = userRepository.save(user);
        assignRoles(saved.getId(), request.getRoleIds(), null);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        SysUser user = findUser(id);
        ensureUsernameAvailable(request.getUsername(), id);
        validateOrganization(request);
        user.updateProfile(
                request.getUsername(),
                request.getDisplayName(),
                request.getEmployeeNo(),
                request.getEmail(),
                request.getPhone(),
                request.getDepartmentId(),
                request.getPositionId(),
                request.getDirectManagerId());
        if (StringUtils.hasText(request.getPassword())) {
            user.changePassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleIds() != null) {
            assignRoles(user.getId(), request.getRoleIds(), null);
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse setUserStatus(Long id, String status) {
        if (!"active".equals(status) && !"disabled".equals(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported user status");
        }
        SysUser user = findUser(id);
        user.setStatus(status);
        return toResponse(user);
    }

    @Transactional
    public UserResponse assignRoles(Long userId, List<Long> roleIds, Long operatorId) {
        SysUser user = findUser(userId);
        Map<Long, SysUserRole> currentByRoleId = new LinkedHashMap<Long, SysUserRole>();
        for (SysUserRole userRole : userRoleRepository.findByUserIdAndDeletedFalse(userId)) {
            currentByRoleId.put(userRole.getRoleId(), userRole);
        }

        List<Long> targetRoleIds = roleIds == null ? new ArrayList<Long>() : roleIds;
        for (Long roleId : targetRoleIds) {
            findRole(roleId);
            if (!currentByRoleId.containsKey(roleId)) {
                userRoleRepository.save(new SysUserRole(userId, roleId));
            }
        }
        for (SysUserRole userRole : currentByRoleId.values()) {
            if (!targetRoleIds.contains(userRole.getRoleId())) {
                userRole.softDelete(operatorId);
            }
        }
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id, Long operatorId) {
        SysUser user = findUser(id);
        user.softDelete(operatorId);
        for (SysUserRole userRole : userRoleRepository.findByUserIdAndDeletedFalse(id)) {
            userRole.softDelete(operatorId);
        }
    }

    private UserResponse toResponse(SysUser user) {
        String departmentName = null;
        if (user.getDepartmentId() != null) {
            OrgDepartment department = departmentRepository.findById(user.getDepartmentId()).orElse(null);
            departmentName = department == null || Boolean.TRUE.equals(department.getDeleted()) ? null : department.getName();
        }
        String positionName = null;
        if (user.getPositionId() != null) {
            OrgPosition position = positionRepository.findById(user.getPositionId()).orElse(null);
            positionName = position == null || Boolean.TRUE.equals(position.getDeleted()) ? null : position.getName();
        }
        String managerName = null;
        if (user.getDirectManagerId() != null) {
            SysUser manager = userRepository.findById(user.getDirectManagerId()).orElse(null);
            managerName = manager == null || Boolean.TRUE.equals(manager.getDeleted()) ? null : manager.getDisplayName();
        }

        List<RoleResponse> roles = new ArrayList<RoleResponse>();
        for (SysUserRole userRole : userRoleRepository.findByUserIdAndDeletedFalse(user.getId())) {
            SysRole role = roleRepository.findById(userRole.getRoleId()).orElse(null);
            if (role != null) {
                roles.add(new RoleResponse(role));
            }
        }
        return new UserResponse(user, departmentName, positionName, managerName, roles);
    }

    private SysUser findUser(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "User not found"));
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "User not found");
        }
        return user;
    }

    private SysRole findRole(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Role not found"));
        if (!Boolean.TRUE.equals(role.getEnabled())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Role disabled");
        }
        return role;
    }

    private void ensureUsernameAvailable(String username, Long currentId) {
        SysUser existing = userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Username already exists");
        }
    }

    private void validateOrganization(UserRequest request) {
        if (request.getDepartmentId() != null) {
            OrgDepartment department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Department not found"));
            if (Boolean.TRUE.equals(department.getDeleted())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Department not found");
            }
        }
        if (request.getPositionId() != null) {
            OrgPosition position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Position not found"));
            if (Boolean.TRUE.equals(position.getDeleted())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Position not found");
            }
        }
        if (request.getDirectManagerId() != null) {
            findUser(request.getDirectManagerId());
        }
    }
}
