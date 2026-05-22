package com.company.approval.system.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.system.domain.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    Optional<SysRole> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    List<SysRole> findByDeletedFalseOrderByIdAsc();
}
