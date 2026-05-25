package com.company.approval.system.application;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        OrgPosition hrPosition = positionRepository.findByCodeAndDeletedFalse("HR")
                .orElseGet(() -> positionRepository.save(new OrgPosition(headOffice.getId(), "HR", "HR")));

        SysRole adminRole = ensureRole("admin", "系统管理员");
        SysRole employeeRole = ensureRole("employee", "普通员工");
        SysRole managerRole = ensureRole("department_manager", "部门主管");
        SysRole financeRole = ensureRole("finance", "财务人员");
        SysRole hrRole = ensureRole("hr", "人事人员");
        SysRole generalManagerRole = ensureRole("general_manager", "总经理");

        Map<String, SysPermission> permissionByCode = new LinkedHashMap<String, SysPermission>();
        String[][] menuPermissions = new String[][] {
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
        for (String[] menu : menuPermissions) {
            permissionByCode.put(menu[0], ensurePermission(menu[0], menu[1], "menu"));
        }
        String[][] operationPermissions = new String[][] {
                {"approval.create", "发起申请"},
                {"approval.save_draft", "保存草稿"},
                {"approval.submit", "提交申请"},
                {"approval.view", "查看申请"},
                {"approval.withdraw", "撤回申请"},
                {"approval.approve", "同意审批"},
                {"approval.reject", "驳回审批"},
                {"approval.transfer", "转交审批"},
                {"approval.add_approver", "加签审批"},
                {"approval.request_more_info", "要求补充材料"},
                {"approval.cc", "抄送审批"},
                {"approval.void", "作废申请"},
                {"approval.export", "导出审批记录"},
                {"organization.manage", "管理组织"},
                {"user.manage", "管理用户"},
                {"role.manage", "管理角色权限"},
                {"workflow.manage", "管理流程配置"},
                {"audit.view", "查看审计日志"},
                {"dashboard.statistics.view", "查看统计看板"}
        };
        for (String[] op : operationPermissions) {
            permissionByCode.put(op[0], ensurePermission(op[0], op[1], "operation"));
        }

        // admin: 全量
        assignPermissions(adminRole, permissionByCode, permissionByCode.keySet().toArray(new String[0]));

        // employee 基础发起申请能力
        assignPermissions(employeeRole, permissionByCode,
                "menu.dashboard", "menu.approvals.new", "menu.approvals.my",
                "menu.approvals.cc", "menu.notifications",
                "approval.create", "approval.save_draft", "approval.submit",
                "approval.view", "approval.withdraw");

        // department_manager / finance / hr 在普通员工基础上加审批动作
        String[] approverCommon = new String[] {
                "menu.dashboard", "menu.approvals.new", "menu.approvals.my",
                "menu.approvals.todo", "menu.approvals.done", "menu.approvals.cc",
                "menu.notifications",
                "approval.create", "approval.save_draft", "approval.submit",
                "approval.view", "approval.withdraw",
                "approval.approve", "approval.reject", "approval.transfer",
                "approval.add_approver", "approval.request_more_info", "approval.cc"
        };
        assignPermissions(managerRole, permissionByCode, approverCommon);
        assignPermissions(financeRole, permissionByCode, approverCommon);
        assignPermissions(hrRole, permissionByCode, approverCommon);

        // general_manager 增加管理列表、导出、统计、审计查看
        assignPermissions(generalManagerRole, permissionByCode,
                "menu.dashboard", "menu.approvals.new", "menu.approvals.my",
                "menu.approvals.todo", "menu.approvals.done", "menu.approvals.cc",
                "menu.approvals.manage", "menu.notifications", "menu.audit_logs",
                "approval.create", "approval.save_draft", "approval.submit",
                "approval.view", "approval.withdraw",
                "approval.approve", "approval.reject", "approval.transfer",
                "approval.add_approver", "approval.request_more_info", "approval.cc",
                "approval.export",
                "dashboard.statistics.view", "audit.view");

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

        SysUser hr = ensureUser("hr01", "HR User", headOffice.getId(), hrPosition.getId(), manager.getId());
        ensureUserRole(hr, hrRole);

        SysUser generalManager = ensureUser("gm01", "General Manager", headOffice.getId(), generalManagerPosition.getId(), null);
        ensureUserRole(generalManager, generalManagerRole);

        SysUser employee = ensureUser("employee01", "Employee User", headOffice.getId(), employeePosition.getId(), manager.getId());
        ensureUserRole(employee, employeeRole);
    }

    private SysPermission ensurePermission(String code, String name, String type) {
        return permissionRepository.findByCodeAndDeletedFalse(code)
                .orElseGet(() -> permissionRepository.save(new SysPermission(code, name, type)));
    }

    private void assignPermissions(SysRole role, Map<String, SysPermission> permissionByCode, String... codes) {
        List<String> codeList = Arrays.asList(codes);
        for (String code : codeList) {
            SysPermission permission = permissionByCode.get(code);
            if (permission == null) {
                continue;
            }
            if (!rolePermissionRepository.existsByRoleIdAndPermissionIdAndDeletedFalse(role.getId(), permission.getId())) {
                rolePermissionRepository.save(new SysRolePermission(role.getId(), permission.getId()));
            }
        }
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
