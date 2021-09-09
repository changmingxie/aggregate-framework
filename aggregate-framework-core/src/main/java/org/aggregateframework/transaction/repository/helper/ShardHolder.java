package org.aggregateframework.transaction.repository.helper;

import java.io.Closeable;
import java.util.List;

public interface ShardHolder<T> extends Closeable {
    List<T> getAllShards();
}
