package org.aggregateframework.remoting;

import org.aggregateframework.remoting.protocol.RemotingCommand;

public interface RemotingServer<T> extends RemotingService<T> {
    RemotingCommand invokeSync(final String key, final RemotingCommand request, final long timeoutMillis);
}
