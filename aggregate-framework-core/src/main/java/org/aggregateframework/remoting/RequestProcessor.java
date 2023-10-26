package org.aggregateframework.remoting;

import org.aggregateframework.remoting.protocol.RemotingCommand;

public interface RequestProcessor<T> {

    RemotingCommand processRequest(T context, RemotingCommand request);
}
