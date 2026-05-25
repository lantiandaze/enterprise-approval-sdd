package com.company.approval.common.pagination;

import java.util.List;

public class PagedResult<T> {

    private final List<T> items;
    private final long totalCount;
    private final int page;
    private final int pageSize;

    public PagedResult(List<T> items, long totalCount, int page, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }
}
