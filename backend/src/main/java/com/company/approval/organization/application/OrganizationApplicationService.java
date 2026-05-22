package com.company.approval.organization.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.organization.domain.OrgDepartment;
import com.company.approval.organization.domain.OrgPosition;
import com.company.approval.organization.dto.DepartmentRequest;
import com.company.approval.organization.dto.DepartmentResponse;
import com.company.approval.organization.dto.PositionRequest;
import com.company.approval.organization.dto.PositionResponse;
import com.company.approval.organization.repository.OrgDepartmentRepository;
import com.company.approval.organization.repository.OrgPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationApplicationService {

    private final OrgDepartmentRepository departmentRepository;
    private final OrgPositionRepository positionRepository;

    public OrganizationApplicationService(
            OrgDepartmentRepository departmentRepository,
            OrgPositionRepository positionRepository) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentTree() {
        List<OrgDepartment> departments = departmentRepository.findByDeletedFalseOrderBySortOrderAscIdAsc();
        Map<Long, DepartmentResponse> responseById = new LinkedHashMap<Long, DepartmentResponse>();
        for (OrgDepartment department : departments) {
            responseById.put(department.getId(), new DepartmentResponse(department));
        }

        List<DepartmentResponse> roots = new ArrayList<DepartmentResponse>();
        for (OrgDepartment department : departments) {
            DepartmentResponse current = responseById.get(department.getId());
            Long parentId = department.getParentId();
            DepartmentResponse parent = parentId == null ? null : responseById.get(parentId);
            if (parent == null) {
                roots.add(current);
            } else {
                parent.getChildren().add(current);
            }
        }
        return roots;
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        ensureDepartmentCodeAvailable(request.getCode(), null);
        ensureParentDepartmentExists(request.getParentId());
        OrgDepartment department = new OrgDepartment(request.getCode(), request.getName());
        department.update(
                request.getCode(),
                request.getName(),
                request.getParentId(),
                request.getLeaderUserId(),
                request.getSortOrder());
        return new DepartmentResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        OrgDepartment department = findDepartment(id);
        ensureDepartmentCodeAvailable(request.getCode(), id);
        ensureParentDepartmentExists(request.getParentId());
        if (id.equals(request.getParentId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Department cannot be its own parent");
        }
        department.update(
                request.getCode(),
                request.getName(),
                request.getParentId(),
                request.getLeaderUserId(),
                request.getSortOrder());
        return new DepartmentResponse(department);
    }

    @Transactional
    public DepartmentResponse setDepartmentEnabled(Long id, boolean enabled) {
        OrgDepartment department = findDepartment(id);
        department.setEnabled(enabled);
        return new DepartmentResponse(department);
    }

    @Transactional
    public void deleteDepartment(Long id, Long operatorId) {
        OrgDepartment department = findDepartment(id);
        if (!departmentRepository.findByParentIdAndDeletedFalse(id).isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Department has child departments");
        }
        if (!positionRepository.findByDepartmentIdAndDeletedFalseOrderBySortOrderAscIdAsc(id).isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Department has positions");
        }
        department.softDelete(operatorId);
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> listPositions(Long departmentId) {
        List<OrgPosition> positions = departmentId == null
                ? positionRepository.findByDeletedFalseOrderBySortOrderAscIdAsc()
                : positionRepository.findByDepartmentIdAndDeletedFalseOrderBySortOrderAscIdAsc(departmentId);
        List<PositionResponse> responses = new ArrayList<PositionResponse>();
        for (OrgPosition position : positions) {
            responses.add(new PositionResponse(position));
        }
        return responses;
    }

    @Transactional
    public PositionResponse createPosition(PositionRequest request) {
        ensurePositionCodeAvailable(request.getCode(), null);
        ensureDepartmentExists(request.getDepartmentId());
        OrgPosition position = new OrgPosition(request.getDepartmentId(), request.getCode(), request.getName());
        position.update(request.getDepartmentId(), request.getCode(), request.getName(), request.getSortOrder());
        return new PositionResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse updatePosition(Long id, PositionRequest request) {
        OrgPosition position = findPosition(id);
        ensurePositionCodeAvailable(request.getCode(), id);
        ensureDepartmentExists(request.getDepartmentId());
        position.update(request.getDepartmentId(), request.getCode(), request.getName(), request.getSortOrder());
        return new PositionResponse(position);
    }

    @Transactional
    public PositionResponse setPositionEnabled(Long id, boolean enabled) {
        OrgPosition position = findPosition(id);
        position.setEnabled(enabled);
        return new PositionResponse(position);
    }

    @Transactional
    public void deletePosition(Long id, Long operatorId) {
        OrgPosition position = findPosition(id);
        position.softDelete(operatorId);
    }

    private OrgDepartment findDepartment(Long id) {
        OrgDepartment department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Department not found"));
        if (Boolean.TRUE.equals(department.getDeleted())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Department not found");
        }
        return department;
    }

    private OrgPosition findPosition(Long id) {
        OrgPosition position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Position not found"));
        if (Boolean.TRUE.equals(position.getDeleted())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Position not found");
        }
        return position;
    }

    private void ensureDepartmentExists(Long id) {
        if (id != null) {
            findDepartment(id);
        }
    }

    private void ensureParentDepartmentExists(Long id) {
        ensureDepartmentExists(id);
    }

    private void ensureDepartmentCodeAvailable(String code, Long currentId) {
        OrgDepartment existing = departmentRepository.findByCodeAndDeletedFalse(code).orElse(null);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Department code already exists");
        }
    }

    private void ensurePositionCodeAvailable(String code, Long currentId) {
        OrgPosition existing = positionRepository.findByCodeAndDeletedFalse(code).orElse(null);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Position code already exists");
        }
    }
}
