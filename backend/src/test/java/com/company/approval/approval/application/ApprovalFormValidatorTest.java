package com.company.approval.approval.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import com.company.approval.approval.dto.ApprovalRequestCommand;
import com.company.approval.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

class ApprovalFormValidatorTest {

    private final ApprovalFormValidator validator = new ApprovalFormValidator();

    @Test
    void validatesLeaveSubmit() {
        ApprovalRequestCommand command = command("leave", "年假申请");
        set(command, "startTime", OffsetDateTime.now());
        set(command, "endTime", OffsetDateTime.now().plusHours(8));
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("leaveType", "annual");
        form.put("reason", "family");
        set(command, "formData", form);

        assertDoesNotThrow(() -> validator.validateSubmit(command));
    }

    @Test
    void rejectsInvalidTimeRange() {
        ApprovalRequestCommand command = command("overtime", "加班申请");
        set(command, "startTime", OffsetDateTime.now());
        set(command, "endTime", OffsetDateTime.now().minusHours(1));
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("reason", "release");
        set(command, "formData", form);

        assertThrows(BusinessException.class, () -> validator.validateSubmit(command));
    }

    @Test
    void rejectsExpenseWithoutPositiveAmount() {
        ApprovalRequestCommand command = command("expense", "报销申请");
        set(command, "amount", BigDecimal.ZERO);
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("expenseCategory", "travel");
        form.put("reason", "client meeting");
        set(command, "formData", form);

        assertThrows(BusinessException.class, () -> validator.validateSubmit(command));
    }

    private ApprovalRequestCommand command(String type, String title) {
        ApprovalRequestCommand command = new ApprovalRequestCommand();
        set(command, "type", type);
        set(command, "title", title);
        set(command, "formData", new HashMap<String, Object>());
        return command;
    }

    private void set(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
