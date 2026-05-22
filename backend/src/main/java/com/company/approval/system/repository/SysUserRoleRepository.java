package com.company.approval.system.repository;

import java.util.List;

import com.company.approval.system.domain.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    boolean existsByUserIdAndRoleIdAndDeletedFalse(Long userId, Long roleId);

    List<SysUserRole> findByUserIdAndDeletedFalse(Long userId);

    @Query("select r.code from SysUserRole ur join SysRole r on ur.roleId = r.id "
            + "where ur.userId = :userId and ur.deleted = false and r.deleted = false and r.enabled = true")
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    @Query("select ur.userId from SysUserRole ur join SysRole r on ur.roleId = r.id "
            + "where r.code = :roleCode and ur.deleted = false and r.deleted = false and r.enabled = true "
            + "order by ur.userId asc")
    List<Long> findUserIdsByRoleCode(@Param("roleCode") String roleCode);
}
