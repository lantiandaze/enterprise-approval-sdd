package com.company.approval.auth.application;

import com.company.approval.security.principal.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class InMemoryUserService implements UserPrincipalService {

    private final PasswordEncoder passwordEncoder;

    public InMemoryUserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserPrincipal loadByUsername(String username) {
        if ("admin".equals(username)) {
            return new UserPrincipal(
                    1L,
                    "admin",
                    "系统管理员",
                    passwordEncoder.encode("admin123"),
                    Collections.singletonList("admin"),
                    Arrays.asList(
                            "menu.dashboard",
                            "menu.approvals.new",
                            "menu.approvals.my",
                            "menu.approvals.todo",
                            "menu.approvals.done",
                            "menu.approvals.cc",
                            "menu.approvals.manage",
                            "menu.organization",
                            "menu.users",
                            "menu.roles",
                            "menu.workflow_config",
                            "menu.notifications",
                            "menu.audit_logs"));
        }
        return null;
    }
}
