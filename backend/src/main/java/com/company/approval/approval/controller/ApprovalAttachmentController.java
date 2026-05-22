package com.company.approval.approval.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.company.approval.approval.application.ApprovalAttachmentService;
import com.company.approval.approval.application.ApprovalAttachmentService.ApprovalAttachmentDownload;
import com.company.approval.approval.dto.ApprovalAttachmentResponse;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/approvals/{requestId}/attachments")
public class ApprovalAttachmentController {

    private final ApprovalAttachmentService attachmentService;
    private final CurrentUserProvider currentUserProvider;

    public ApprovalAttachmentController(ApprovalAttachmentService attachmentService, CurrentUserProvider currentUserProvider) {
        this.attachmentService = attachmentService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ApiResponse<List<ApprovalAttachmentResponse>> list(@PathVariable Long requestId) {
        return ApiResponse.success(attachmentService.list(requestId, currentUserProvider.getCurrentUser()));
    }

    @PostMapping
    public ApiResponse<ApprovalAttachmentResponse> upload(@PathVariable Long requestId, @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(attachmentService.upload(requestId, file, currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long requestId, @PathVariable Long attachmentId) {
        ApprovalAttachmentDownload download = attachmentService.getDownload(requestId, attachmentId, currentUserProvider.getCurrentUser());
        String encodedFileName = encodeFileName(download.getAttachment().getFileName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(new FileSystemResource(download.getFilePath().toFile()));
    }

    @DeleteMapping("/{attachmentId}")
    public ApiResponse<Void> delete(@PathVariable Long requestId, @PathVariable Long attachmentId) {
        attachmentService.delete(requestId, attachmentId, currentUserProvider.getCurrentUser());
        return ApiResponse.success(null);
    }

    private String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return fileName;
        }
    }
}
