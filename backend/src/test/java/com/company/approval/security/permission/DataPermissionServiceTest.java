package com.company.approval.security.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import com.company.approval.security.principal.UserPrincipal;
import com.company.approval.system.domain.SysUser;
import com.company.approval.system.repository.SysUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataPermissionServiceTest {

    @Test
    void adminCanSeeAll() {
        SysUserRepository users = Mockito.mock(SysUserRepository.class);
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        DataPermissionDecision decision = new DataPermissionService(users).decide(principal(1L, "admin"));

        assertEquals(DataPermissionScope.ALL, decision.getScope());
        assertFalse(decision.hasApprovalTypeRestriction());
    }

    @Test
    void departmentManagerSeesDepartment() {
        SysUserRepository users = Mockito.mock(SysUserRepository.class);
        SysUser user = user(2L);
        user.updateProfile(user.getUsername(), user.getDisplayName(), null, null, null, 10L, null, null);
        when(users.findById(2L)).thenReturn(Optional.of(user));
        DataPermissionDecision decision = new DataPermissionService(users).decide(principal(2L, "department_manager"));

        assertEquals(DataPermissionScope.DEPARTMENT, decision.getScope());
        assertEquals(10L, decision.getDepartmentId());
        assertFalse(decision.hasApprovalTypeRestriction());
    }

    @Test
    void employeeSeesSelf() {
        SysUserRepository users = Mockito.mock(SysUserRepository.class);
        when(users.findById(3L)).thenReturn(Optional.of(user(3L)));
        DataPermissionDecision decision = new DataPermissionService(users).decide(principal(3L, "employee"));

        assertEquals(DataPermissionScope.SELF, decision.getScope());
        assertFalse(decision.hasApprovalTypeRestriction());
    }

    @Test
    void financeOnlySeesExpenseAcrossAll() {
        SysUserRepository users = Mockito.mock(SysUserRepository.class);
        when(users.findById(4L)).thenReturn(Optional.of(user(4L)));
        DataPermissionDecision decision = new DataPermissionService(users).decide(principal(4L, "finance"));

        assertEquals(DataPermissionScope.ALL, decision.getScope());
        assertTrue(decision.hasApprovalTypeRestriction());
        assertEquals(java.util.Collections.singleton("expense"), decision.getAllowedApprovalTypes());
    }

    @Test
    void hrOnlySeesLeaveAndOvertimeAcrossAll() {
        SysUserRepository users = Mockito.mock(SysUserRepository.class);
        when(users.findById(5L)).thenReturn(Optional.of(user(5L)));
        DataPermissionDecision decision = new DataPermissionService(users).decide(principal(5L, "hr"));

        assertEquals(DataPermissionScope.ALL, decision.getScope());
        assertTrue(decision.hasApprovalTypeRestriction());
        assertTrue(decision.getAllowedApprovalTypes().contains("leave"));
        assertTrue(decision.getAllowedApprovalTypes().contains("overtime"));
        assertFalse(decision.getAllowedApprovalTypes().contains("expense"));
    }

    @Test
    void generalManagerSeesAllWithoutTypeRestriction() {
        SysUserRepository users = Mockito.mock(SysUserRepository.class);
        when(users.findById(6L)).thenReturn(Optional.of(user(6L)));
        DataPermissionDecision decision = new DataPermissionService(users).decide(principal(6L, "general_manager"));

        assertEquals(DataPermissionScope.ALL, decision.getScope());
        assertFalse(decision.hasApprovalTypeRestriction());
    }

    private UserPrincipal principal(Long id, String role) {
        return new UserPrincipal(id, "u" + id, "User " + id, "pwd", Arrays.asList(role), Arrays.asList("menu.dashboard"));
    }

    private SysUser user(Long id) {
        SysUser user = new SysUser("u" + id, "pwd", "User " + id);
        setId(user, id);
        return user;
    }

    private void setId(SysUser user, Long id) {
        try {
            java.lang.reflect.Field field = SysUser.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
