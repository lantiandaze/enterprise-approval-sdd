package com.company.approval.notification.dto;

public class UnreadCountResponse {

    private long count;

    public UnreadCountResponse(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
