package com.company.approval.notification.controller;

import java.util.List;

import com.company.approval.common.response.ApiResponse;
import com.company.approval.notification.application.NotificationApplicationService;
import com.company.approval.notification.dto.NotificationResponse;
import com.company.approval.notification.dto.UnreadCountResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationApplicationService service;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationApplicationService service, CurrentUserProvider currentUserProvider) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> listMine() {
        return ApiResponse.success(service.listMine(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount() {
        return ApiResponse.success(service.unreadCount(currentUserProvider.getCurrentUser()));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id) {
        return ApiResponse.success(service.markRead(id, currentUserProvider.getCurrentUser()));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        service.markAllRead(currentUserProvider.getCurrentUser());
        return ApiResponse.success(null);
    }
}
