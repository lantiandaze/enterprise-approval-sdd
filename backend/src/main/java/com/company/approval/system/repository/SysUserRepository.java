package com.company.approval.system.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.system.domain.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsernameAndDeletedFalse(String username);

    boolean existsByUsernameAndDeletedFalse(String username);

    List<SysUser> findByDeletedFalseOrderByIdAsc();
}
