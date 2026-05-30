package com.example.fitness.common.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class PageResponse<T> {

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private long pages;

    public static <T> PageResponse<T> of(List<T> list, long total, int pageNum, int pageSize) {
        List<T> safeList = list == null ? Collections.emptyList() : list;
        long safeTotal = Math.max(total, 0L);
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        long pages = safeTotal == 0 ? 0 : (safeTotal + safePageSize - 1) / safePageSize;
        return PageResponse.<T>builder()
                .list(safeList)
                .total(safeTotal)
                .pageNum(safePageNum)
                .pageSize(safePageSize)
                .pages(pages)
                .build();
    }
}
