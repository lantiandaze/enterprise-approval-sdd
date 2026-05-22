package com.company.approval.system.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.system.domain.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    Optional<SysPermission> findByCodeAndDeletedFalse(String code);

    List<SysPermission> findByDeletedFalseOrderBySortOrderAscIdAsc();
}
