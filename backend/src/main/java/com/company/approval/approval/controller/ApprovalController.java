package com.company.approval.approval.controller;

import java.util.List;
import javax.validation.Valid;

import com.company.approval.approval.application.ApprovalApplicationService;
import com.company.approval.approval.application.ApprovalWorkflowService;
import com.company.approval.approval.dto.ApprovalCcResponse;
import com.company.approval.approval.dto.ApprovalTaskActionRequest;
import com.company.approval.approval.dto.ApprovalTaskResponse;
import com.company.approval.approval.dto.ApprovalRequestCommand;
import com.company.approval.approval.dto.ApprovalRequestResponse;
import com.company.approval.common.response.ApiResponse;
import com.company.approval.security.principal.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalApplicationService approvalApplicationService;
    private final ApprovalWorkflowService workflowService;
    private final CurrentUserProvider currentUserProvider;

    public ApprovalController(
            ApprovalApplicationService approvalApplicationService,
            ApprovalWorkflowService workflowService,
            CurrentUserProvider currentUserProvider) {
        this.approvalApplicationService = approvalApplicationService;
        this.workflowService = workflowService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/drafts")
    public ApiResponse<ApprovalRequestResponse> saveDraft(@Valid @RequestBody ApprovalRequestCommand command) {
        return ApiResponse.success(approvalApplicationService.saveDraft(command, currentUserProvider.getCurrentUser()));
    }

    @PostMapping
    public ApiResponse<ApprovalRequestResponse> submitNew(@Valid @RequestBody ApprovalRequestCommand command) {
        return ApiResponse.success(approvalApplicationService.submitNew(command, currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<ApprovalRequestResponse> submitDraft(@PathVariable Long id, @Valid @RequestBody ApprovalRequestCommand command) {
        return ApiResponse.success(approvalApplicationService.submitDraft(id, command, currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/{id}/resubmit")
    public ApiResponse<ApprovalRequestResponse> resubmitMoreInfo(@PathVariable Long id, @Valid @RequestBody ApprovalRequestCommand command) {
        return ApiResponse.success(approvalApplicationService.resubmitMoreInfo(id, command, null, currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(@PathVariable Long id, @RequestBody(required = false) ApprovalTaskActionRequest request) {
        String comment = request == null ? null : request.getComment();
        workflowService.withdraw(id, comment, currentUserProvider.getCurrentUser());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/void")
    public ApiResponse<Void> voidRequest(@PathVariable Long id, @RequestBody(required = false) ApprovalTaskActionRequest request) {
        String comment = request == null ? null : request.getComment();
        workflowService.voidRequest(id, comment, currentUserProvider.getCurrentUser());
        return ApiResponse.success(null);
    }

    @GetMapping("/mine")
    public ApiResponse<List<ApprovalRequestResponse>> listMine() {
        return ApiResponse.success(approvalApplicationService.listMine(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApprovalRequestResponse> getMine(@PathVariable Long id) {
        return ApiResponse.success(approvalApplicationService.getMine(id, currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/todo")
    public ApiResponse<List<ApprovalTaskResponse>> listTodo() {
        return ApiResponse.success(workflowService.listTodo(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/done")
    public ApiResponse<List<ApprovalTaskResponse>> listDone() {
        return ApiResponse.success(workflowService.listDone(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/cc")
    public ApiResponse<List<ApprovalCcResponse>> listCc() {
        return ApiResponse.success(workflowService.listCc(currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/tasks/{taskId}/approve")
    public ApiResponse<ApprovalTaskResponse> approve(@PathVariable Long taskId, @RequestBody(required = false) ApprovalTaskActionRequest request) {
        String comment = request == null ? null : request.getComment();
        return ApiResponse.success(workflowService.approve(taskId, comment, currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/tasks/{taskId}/reject")
    public ApiResponse<ApprovalTaskResponse> reject(@PathVariable Long taskId, @RequestBody(required = false) ApprovalTaskActionRequest request) {
        String comment = request == null ? null : request.getComment();
        return ApiResponse.success(workflowService.reject(taskId, comment, currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/tasks/{taskId}/request-more-info")
    public ApiResponse<ApprovalTaskResponse> requestMoreInfo(@PathVariable Long taskId, @RequestBody(required = false) ApprovalTaskActionRequest request) {
        String comment = request == null ? null : request.getComment();
        return ApiResponse.success(workflowService.requestMoreInfo(taskId, comment, currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/tasks/{taskId}/transfer")
    public ApiResponse<ApprovalTaskResponse> transfer(@PathVariable Long taskId, @RequestBody ApprovalTaskActionRequest request) {
        return ApiResponse.success(workflowService.transfer(taskId, request.getTargetUserId(), request.getComment(), currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/tasks/{taskId}/add-signer")
    public ApiResponse<ApprovalTaskResponse> addSigner(@PathVariable Long taskId, @RequestBody ApprovalTaskActionRequest request) {
        return ApiResponse.success(workflowService.addSigner(taskId, request.getTargetUserId(), request.getComment(), currentUserProvider.getCurrentUser()));
    }

    @PostMapping("/tasks/{taskId}/cc")
    public ApiResponse<ApprovalTaskResponse> cc(@PathVariable Long taskId, @RequestBody ApprovalTaskActionRequest request) {
        return ApiResponse.success(workflowService.cc(taskId, request.getTargetUserIds(), request.getComment(), currentUserProvider.getCurrentUser()));
    }
}
