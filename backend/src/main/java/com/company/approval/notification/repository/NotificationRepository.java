package com.company.approval.notification.repository;

import java.util.List;
import java.util.Optional;

import com.company.approval.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndDeletedFalse(Long id);

    long countByUserIdAndReadAtIsNullAndDeletedFalse(Long userId);

    List<Notification> findByUserIdAndReadAtIsNullAndDeletedFalse(Long userId);

    boolean existsByRelatedTaskIdAndTypeAndDeletedFalse(Long relatedTaskId, String type);
}
