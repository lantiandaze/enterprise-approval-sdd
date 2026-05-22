package com.company.approval.approval.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import com.company.approval.approval.dto.ApprovalRequestCommand;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApprovalFormValidator {

    public void validateDraft(ApprovalRequestCommand command) {
        requireType(command.getType());
        if (!StringUtils.hasText(command.getTitle())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Title is required");
        }
    }

    public void validateSubmit(ApprovalRequestCommand command) {
        validateDraft(command);
        Map<String, Object> form = command.getFormData();
        if ("leave".equals(command.getType())) {
            requireTimeRange(command.getStartTime(), command.getEndTime());
            requireText(form, "leaveType");
            requireText(form, "reason");
        } else if ("expense".equals(command.getType())) {
            requirePositiveAmount(command.getAmount());
            requireText(form, "expenseCategory");
            requireText(form, "reason");
        } else if ("purchase".equals(command.getType())) {
            requirePositiveAmount(command.getAmount());
            requireText(form, "itemName");
            requireText(form, "reason");
        } else if ("overtime".equals(command.getType())) {
            requireTimeRange(command.getStartTime(), command.getEndTime());
            requireText(form, "reason");
        } else if ("business_trip".equals(command.getType())) {
            requireTimeRange(command.getStartTime(), command.getEndTime());
            requireText(form, "destination");
            requireText(form, "reason");
        }
    }

    private void requireType(String type) {
        if (!"leave".equals(type)
                && !"expense".equals(type)
                && !"purchase".equals(type)
                && !"overtime".equals(type)
                && !"business_trip".equals(type)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported approval type");
        }
    }

    private void requireTimeRange(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid time range");
        }
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Amount must be positive");
        }
    }

    private void requireText(Map<String, Object> form, String key) {
        Object value = form == null ? null : form.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, key + " is required");
        }
    }
}
