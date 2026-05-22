package com.company.approval.approval.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.company.approval.approval.domain.ApprovalActionLog;
import com.company.approval.approval.domain.ApprovalAttachment;
import com.company.approval.approval.domain.ApprovalRequest;
import com.company.approval.approval.dto.ApprovalAttachmentResponse;
import com.company.approval.approval.repository.ApprovalActionLogRepository;
import com.company.approval.approval.repository.ApprovalAttachmentRepository;
import com.company.approval.approval.repository.ApprovalRequestRepository;
import com.company.approval.common.exception.BusinessException;
import com.company.approval.common.exception.ErrorCode;
import com.company.approval.security.principal.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ApprovalAttachmentService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private final ApprovalRequestRepository requestRepository;
    private final ApprovalAttachmentRepository attachmentRepository;
    private final ApprovalActionLogRepository actionLogRepository;
    private final Path storageRoot;

    public ApprovalAttachmentService(
            ApprovalRequestRepository requestRepository,
            ApprovalAttachmentRepository attachmentRepository,
            ApprovalActionLogRepository actionLogRepository,
            @Value("${app.attachments.storage-path:./storage/attachments}") String storagePath) {
        this.requestRepository = requestRepository;
        this.attachmentRepository = attachmentRepository;
        this.actionLogRepository = actionLogRepository;
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<ApprovalAttachmentResponse> list(Long requestId, UserPrincipal principal) {
        assertRequestOwner(requestId, principal);
        List<ApprovalAttachmentResponse> responses = new ArrayList<ApprovalAttachmentResponse>();
        for (ApprovalAttachment attachment : attachmentRepository.findByRequestIdAndDeletedFalseOrderByCreatedAtAsc(requestId)) {
            responses.add(new ApprovalAttachmentResponse(attachment));
        }
        return responses;
    }

    @Transactional
    public ApprovalAttachmentResponse upload(Long requestId, MultipartFile file, UserPrincipal principal) {
        ApprovalRequest request = assertRequestOwner(requestId, principal);
        validateFile(file);
        try {
            Files.createDirectories(storageRoot);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
            if (originalName.contains("..")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid file name");
            }
            String storedName = UUID.randomUUID().toString() + extensionOf(originalName);
            Path target = storageRoot.resolve(storedName).normalize();
            if (!target.startsWith(storageRoot)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid storage path");
            }
            file.transferTo(target.toFile());
            ApprovalAttachment attachment = new ApprovalAttachment(
                    request.getId(),
                    originalName,
                    storedName,
                    file.getContentType(),
                    file.getSize(),
                    principal.getUserId());
            ApprovalAttachment saved = attachmentRepository.save(attachment);
            actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "upload_attachment", request.getStatus(), request.getStatus(), originalName));
            return new ApprovalAttachmentResponse(saved);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Attachment upload failed");
        }
    }

    @Transactional(readOnly = true)
    public ApprovalAttachmentDownload getDownload(Long requestId, Long attachmentId, UserPrincipal principal) {
        assertRequestOwner(requestId, principal);
        ApprovalAttachment attachment = findActiveAttachment(requestId, attachmentId);
        Path filePath = storageRoot.resolve(attachment.getStoragePath()).normalize();
        if (!filePath.startsWith(storageRoot) || !Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Attachment file not found");
        }
        return new ApprovalAttachmentDownload(attachment, filePath);
    }

    @Transactional
    public void delete(Long requestId, Long attachmentId, UserPrincipal principal) {
        ApprovalRequest request = assertRequestOwner(requestId, principal);
        ApprovalAttachment attachment = findActiveAttachment(requestId, attachmentId);
        attachment.softDelete(principal.getUserId());
        actionLogRepository.save(new ApprovalActionLog(request.getId(), principal.getUserId(), principal.getDisplayName(), "delete_attachment", request.getStatus(), request.getStatus(), attachment.getFileName()));
    }

    private ApprovalRequest assertRequestOwner(Long requestId, UserPrincipal principal) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Approval request not found"));
        if (Boolean.TRUE.equals(request.getDeleted()) || !principal.getUserId().equals(request.getApplicantId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return request;
    }

    private ApprovalAttachment findActiveAttachment(Long requestId, Long attachmentId) {
        ApprovalAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Attachment not found"));
        if (Boolean.TRUE.equals(attachment.getDeleted()) || !requestId.equals(attachment.getRequestId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Attachment not found");
        }
        return attachment;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Attachment file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Attachment file is too large");
        }
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }

    public static class ApprovalAttachmentDownload {
        private final ApprovalAttachment attachment;
        private final Path filePath;

        public ApprovalAttachmentDownload(ApprovalAttachment attachment, Path filePath) {
            this.attachment = attachment;
            this.filePath = filePath;
        }

        public ApprovalAttachment getAttachment() { return attachment; }
        public Path getFilePath() { return filePath; }
    }
}
