package com.company.approval.notification.application;

import java.util.ArrayList;
import java.util.List;

import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.notification.domain.Notification;
import com.company.approval.notification.dto.NotificationResponse;
import com.company.approval.notification.dto.UnreadCountResponse;
import com.company.approval.notification.repository.NotificationRepository;
import com.company.approval.security.principal.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;

    public NotificationApplicationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(UserPrincipal principal) {
        List<NotificationResponse> responses = new ArrayList<NotificationResponse>();
        for (Notification notification : notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(principal.getUserId())) {
            responses.add(new NotificationResponse(notification));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(UserPrincipal principal) {
        return new UnreadCountResponse(notificationRepository.countByUserIdAndReadAtIsNullAndDeletedFalse(principal.getUserId()));
    }

    @Transactional
    public NotificationResponse markRead(Long id, UserPrincipal principal) {
        Notification notification = notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Notification not found"));
        if (!principal.getUserId().equals(notification.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        notification.markRead(principal.getUserId());
        return new NotificationResponse(notification);
    }

    @Transactional
    public void markAllRead(UserPrincipal principal) {
        for (Notification notification : notificationRepository.findByUserIdAndReadAtIsNullAndDeletedFalse(principal.getUserId())) {
            notification.markRead(principal.getUserId());
        }
    }

    @Transactional
    public void create(Long userId, String type, String title, String content, Long requestId, Long taskId, Long operatorId) {
        notificationRepository.save(new Notification(userId, type, title, content, requestId, taskId, operatorId));
    }

    @Transactional(readOnly = true)
    public boolean existsForTask(Long taskId, String type) {
        return notificationRepository.existsByRelatedTaskIdAndTypeAndDeletedFalse(taskId, type);
    }
}
