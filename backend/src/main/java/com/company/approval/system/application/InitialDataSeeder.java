package com.company.approval.system.application;

import com.company.approval.organization.domain.OrgDepartment;
import com.company.approval.organization.domain.OrgPosition;
import com.company.approval.organization.repository.OrgDepartmentRepository;
import com.company.approval.organization.repository.OrgPositionRepository;
import com.company.approval.system.domain.SysPermission;
import com.company.approval.system.domain.SysRole;
import com.company.approval.system.domain.SysRolePermission;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.domain.SysUserRole;
import com.company.approval.system.repository.SysPermissionRepository;
import com.company.approval.system.repository.SysRolePermissionRepository;
import com.company.approval.system.repository.SysRoleRepository;
import com.company.approval.system.repository.SysUserRepository;
import com.company.approval.system.repository.SysUserRoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("postgres")
public class InitialDataSeeder implements ApplicationRunner {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final OrgDepartmentRepository departmentRepository;
    private final OrgPositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataSeeder(
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            SysPermissionRepository permissionRepository,
            SysUserRoleRepository userRoleRepository,
            SysRolePermissionRepository rolePermissionRepository,
            OrgDepartmentRepository departmentRepository,
            OrgPositionRepository positionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        OrgDepartment headOffice = departmentRepository.findByCodeAndDeletedFalse("HQ")
                .orElseGet(() -> departmentRepository.save(new OrgDepartment("HQ", "Head Office")));
        OrgPosition generalManagerPosition = positionRepository.findByCodeAndDeletedFalse("GENERAL_MANAGER")
                .orElseGet(() -> positionRepository.save(new OrgPosition(headOffice.getId(), "GENERAL_MANAGER", "General Manager")));
        OrgPosition employeePosition = positionRepository.findByCodeAndDeletedFalse("EMPLOYEE")
                .orElseGet(() -> positionRepository.save(new OrgPosition(headOffice.getId(), "EMPLOYEE", "Employee")));
        OrgPosition managerPosition = positionRepository.findByCodeAndDeletedFalse("DEPARTMENT_MANAGER")
                .orElseGet(() -> positionRepository.save(new OrgPosition(headOffice.getId(), "DEPARTMENT_MANAGER", "Department Manager")));
        OrgPosition financePosition = positionRepository.findByCodeAndDeletedFalse("FINANCE")
                .orElseGet(() -> positionRepository.save(new OrgPosition(headOffice.getId(), "FINANCE", "Finance")));

        SysRole adminRole = ensureRole("admin", "系统管理员");
        SysRole employeeRole = ensureRole("employee", "普通员工");
        SysRole managerRole = ensureRole("department_manager", "部门主管");
        SysRole financeRole = ensureRole("finance", "财务人员");
        ensureRole("hr", "人事人员");
        SysRole generalManagerRole = ensureRole("general_manager", "总经理");

        String[][] permissions = new String[][] {
                {"menu.dashboard", "工作台"},
                {"menu.approvals.new", "发起申请"},
                {"menu.approvals.my", "我的申请"},
                {"menu.approvals.todo", "我的待办"},
                {"menu.approvals.done", "我的已办"},
                {"menu.approvals.cc", "抄送我的"},
                {"menu.approvals.manage", "审批管理"},
                {"menu.organization", "组织架构"},
                {"menu.users", "用户管理"},
                {"menu.roles", "角色权限"},
                {"menu.workflow_config", "流程配置"},
                {"menu.notifications", "通知中心"},
                {"menu.audit_logs", "审计日志"}
        };
        for (String[] permissionConfig : permissions) {
            String permissionCode = permissionConfig[0];
            String permissionName = permissionConfig[1];
            SysPermission permission = permissionRepository.findByCodeAndDeletedFalse(permissionCode)
                    .orElseGet(() -> permissionRepository.save(new SysPermission(permissionCode, permissionName, "menu")));
            if (!rolePermissionRepository.existsByRoleIdAndPermissionIdAndDeletedFalse(adminRole.getId(), permission.getId())) {
                rolePermissionRepository.save(new SysRolePermission(adminRole.getId(), permission.getId()));
            }
        }

        SysUser admin = userRepository.findByUsernameAndDeletedFalse("admin")
                .orElseGet(() -> userRepository.save(new SysUser(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "System Administrator")));
        if (!userRoleRepository.existsByUserIdAndRoleIdAndDeletedFalse(admin.getId(), adminRole.getId())) {
            userRoleRepository.save(new SysUserRole(admin.getId(), adminRole.getId()));
        }

        SysUser manager = ensureUser("manager01", "Department Manager", headOffice.getId(), managerPosition.getId(), null);
        ensureUserRole(manager, managerRole);

        SysUser finance = ensureUser("finance01", "Finance User", headOffice.getId(), financePosition.getId(), manager.getId());
        ensureUserRole(finance, financeRole);

        SysUser generalManager = ensureUser("gm01", "General Manager", headOffice.getId(), generalManagerPosition.getId(), null);
        ensureUserRole(generalManager, generalManagerRole);

        SysUser employee = ensureUser("employee01", "Employee User", headOffice.getId(), employeePosition.getId(), manager.getId());
        ensureUserRole(employee, employeeRole);
    }

    private SysRole ensureRole(String code, String name) {
        return roleRepository.findByCodeAndDeletedFalse(code)
                .orElseGet(() -> roleRepository.save(new SysRole(code, name, true)));
    }

    private SysUser ensureUser(String username, String displayName, Long departmentId, Long positionId, Long directManagerId) {
        SysUser user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseGet(() -> userRepository.save(new SysUser(
                        username,
                        passwordEncoder.encode("admin123"),
                        displayName)));
        user.updateProfile(username, displayName, null, null, null, departmentId, positionId, directManagerId);
        user.setStatus("active");
        return user;
    }

    private void ensureUserRole(SysUser user, SysRole role) {
        if (!userRoleRepository.existsByUserIdAndRoleIdAndDeletedFalse(user.getId(), role.getId())) {
            userRoleRepository.save(new SysUserRole(user.getId(), role.getId()));
        }
    }
}
