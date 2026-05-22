package com.company.approval.system.repository;

import java.util.List;

import com.company.approval.system.domain.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

    boolean existsByRoleIdAndPermissionIdAndDeletedFalse(Long roleId, Long permissionId);

    List<SysRolePermission> findByRoleIdAndDeletedFalse(Long roleId);

    @Query("select p.id from SysRolePermission rp join SysPermission p on rp.permissionId = p.id "
            + "where rp.roleId = :roleId and rp.deleted = false and p.deleted = false")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Query("select distinct p.code from SysUserRole ur "
            + "join SysRolePermission rp on ur.roleId = rp.roleId "
            + "join SysPermission p on rp.permissionId = p.id "
            + "where ur.userId = :userId "
            + "and ur.deleted = false and rp.deleted = false and p.deleted = false and p.enabled = true")
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
