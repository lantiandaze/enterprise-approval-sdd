package com.company.approval.organization.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.organization.domain.OrgPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgPositionRepository extends JpaRepository<OrgPosition, Long> {

    Optional<OrgPosition> findByCodeAndDeletedFalse(String code);

    List<OrgPosition> findByDeletedFalseOrderBySortOrderAscIdAsc();

    List<OrgPosition> findByDepartmentIdAndDeletedFalseOrderBySortOrderAscIdAsc(Long departmentId);

    boolean existsByCodeAndDeletedFalse(String code);
}
