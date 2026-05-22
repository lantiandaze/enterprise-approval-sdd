package com.company.approval.common.pagination;

public class PageQuery {

    private int page = 1;
    private int pageSize = 20;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(page, 1);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            this.pageSize = 20;
            return;
        }
        this.pageSize = Math.min(pageSize, 100);
    }
}

