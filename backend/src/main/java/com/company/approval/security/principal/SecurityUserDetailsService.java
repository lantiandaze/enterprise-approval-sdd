package com.company.approval.security.principal;

import com.company.approval.auth.application.UserPrincipalService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserPrincipalService userPrincipalService;

    public SecurityUserDetailsService(UserPrincipalService userPrincipalService) {
        this.userPrincipalService = userPrincipalService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserPrincipal principal = userPrincipalService.loadByUsername(username);
        if (principal == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return principal;
    }
}
