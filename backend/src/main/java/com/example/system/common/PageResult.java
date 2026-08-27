package com.example.system.common;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResult<T> {

    private List<T> list;
    private long total;

    public PageResult() {
    }

    public PageResult(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
