package org.aggregateframework.session;

import org.aggregateframework.context.IdentifiedEntityMap;

/**
 * Created by changmingxie on 7/17/15.
 */
public class AggregateContext {

    private IdentifiedEntityMap entityMap = new IdentifiedEntityMap();

    public IdentifiedEntityMap getEntityMap() {
        return entityMap;
    }

    private boolean isAggregateChanged;

    public boolean isAggregateChanged() {
        return isAggregateChanged;
    }

    public void setAggregateChanged(boolean isAggregateDirty) {
        this.isAggregateChanged = isAggregateDirty;
    }
}
