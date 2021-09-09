package org.aggregateframework.transaction.server.model;

import lombok.Data;

import java.util.List;

/**
 * Created by Lee on 2020/4/8 18:38.
 */
@Data
public class ResetRequest {
    
    private String       domain;
    private String       row;
    private List<String> keys;
    
}
