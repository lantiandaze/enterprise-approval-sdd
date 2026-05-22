package com.company.approval.approval.dto;

import java.util.Map;

public class StatisticsResponse {

    private Map<String, Long> typeCounts;
    private Map<String, Long> statusCounts;
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
    private long overdueTaskCount;

    public StatisticsResponse(Map<String, Long> typeCounts, Map<String, Long> statusCounts, long pendingCount, long approvedCount, long rejectedCount, long overdueTaskCount) {
        this.typeCounts = typeCounts;
        this.statusCounts = statusCounts;
        this.pendingCount = pendingCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.overdueTaskCount = overdueTaskCount;
    }

    public Map<String, Long> getTypeCounts() { return typeCounts; }
    public Map<String, Long> getStatusCounts() { return statusCounts; }
    public long getPendingCount() { return pendingCount; }
    public long getApprovedCount() { return approvedCount; }
    public long getRejectedCount() { return rejectedCount; }
    public long getOverdueTaskCount() { return overdueTaskCount; }
}
