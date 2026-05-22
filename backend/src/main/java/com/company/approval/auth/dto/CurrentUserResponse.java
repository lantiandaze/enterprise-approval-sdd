package com.company.approval.auth.dto;

import com.company.approval.security.principal.UserPrincipal;
import java.util.List;

public class CurrentUserResponse {

    private Long id;
    private String username;
    private String displayName;
    private List<String> roles;
    private List<String> permissions;

    public CurrentUserResponse(Long id, String username, String displayName, List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.roles = roles;
        this.permissions = permissions;
    }

    public static CurrentUserResponse from(UserPrincipal principal) {
        return new CurrentUserResponse(
                principal.getUserId(),
                principal.getUsername(),
                principal.getDisplayName(),
                principal.getRoles(),
                principal.getPermissions());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}

