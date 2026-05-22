package com.company.approval.auth.application;

import com.company.approval.security.principal.UserPrincipal;

public interface UserPrincipalService {

    UserPrincipal loadByUsername(String username);
}
