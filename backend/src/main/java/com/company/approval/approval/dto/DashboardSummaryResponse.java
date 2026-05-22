package com.company.approval.approval.dto;

public class DashboardSummaryResponse {

    private long myTodoCount;
    private long myInProgressCount;
    private long unreadNotificationCount;
    private long globalPendingCount;
    private long todaySubmittedCount;

    public DashboardSummaryResponse(long myTodoCount, long myInProgressCount, long unreadNotificationCount, long globalPendingCount, long todaySubmittedCount) {
        this.myTodoCount = myTodoCount;
        this.myInProgressCount = myInProgressCount;
        this.unreadNotificationCount = unreadNotificationCount;
        this.globalPendingCount = globalPendingCount;
        this.todaySubmittedCount = todaySubmittedCount;
    }

    public long getMyTodoCount() { return myTodoCount; }
    public long getMyInProgressCount() { return myInProgressCount; }
    public long getUnreadNotificationCount() { return unreadNotificationCount; }
    public long getGlobalPendingCount() { return globalPendingCount; }
    public long getTodaySubmittedCount() { return todaySubmittedCount; }
}
