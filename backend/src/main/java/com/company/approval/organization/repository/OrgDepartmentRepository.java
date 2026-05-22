package com.company.approval.organization.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.organization.domain.OrgDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgDepartmentRepository extends JpaRepository<OrgDepartment, Long> {

    Optional<OrgDepartment> findByCodeAndDeletedFalse(String code);

    List<OrgDepartment> findByDeletedFalseOrderBySortOrderAscIdAsc();

    List<OrgDepartment> findByParentIdAndDeletedFalse(Long parentId);

    boolean existsByCodeAndDeletedFalse(String code);
}
