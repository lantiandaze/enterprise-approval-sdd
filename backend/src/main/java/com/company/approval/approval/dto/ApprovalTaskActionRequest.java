package com.company.approval.approval.dto;

public class ApprovalTaskActionRequest {

    private String comment;
    private Long targetUserId;
    private java.util.List<Long> targetUserIds;

    public String getComment() {
        return comment;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public java.util.List<Long> getTargetUserIds() {
        return targetUserIds;
    }
}
