package org.aggregateframework.repository;

import java.util.Collection;

/**
 * User: changming.xie
 * Date: 14-7-25
 * Time: 下午2:43
 */
public interface SaveAggregateCallback<T> {
    public void save(final Collection<T> aggregate);
}
