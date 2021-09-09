package org.aggregateframework.transaction.server.model;

import lombok.Data;

import java.util.List;

/**
 * Created by cheng.zeng on 2016/9/2.
 */
@Data
public class Page<T> {
    
    private List<T> items;
    private Integer pageNum;
    private Integer pageSize;
    private int     pages;
    private int     total;
    
    
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    public static <T> Page<T> empty() {
        return new Page<>();
    }
}
