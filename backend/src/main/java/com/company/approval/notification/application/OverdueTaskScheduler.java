package com.company.approval.notification.application;

import java.time.OffsetDateTime;

import com.company.approval.approval.domain.ApprovalRequest;
import com.company.approval.approval.domain.ApprovalTask;
import com.company.approval.approval.repository.ApprovalRequestRepository;
import com.company.approval.approval.repository.ApprovalTaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OverdueTaskScheduler {

    private final ApprovalTaskRepository taskRepository;
    private final ApprovalRequestRepository requestRepository;
    private final NotificationApplicationService notificationService;

    public OverdueTaskScheduler(
            ApprovalTaskRepository taskRepository,
            ApprovalRequestRepository requestRepository,
            NotificationApplicationService notificationService) {
        this.taskRepository = taskRepository;
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void scanOverdueTasks() {
        for (ApprovalTask task : taskRepository.findByStatusAndOverdueFalseAndDueAtBeforeAndDeletedFalse("pending", OffsetDateTime.now())) {
            task.markOverdue(0L);
            ApprovalRequest request = requestRepository.findById(task.getRequestId()).orElse(null);
            String title = request == null ? "审批待办已超时" : "审批待办已超时：" + request.getTitle();
            if (!notificationService.existsForTask(task.getId(), "overdue")) {
                notificationService.create(task.getAssigneeId(), "overdue", "审批待办已超时", title, task.getRequestId(), task.getId(), 0L);
            }
        }
    }
}
