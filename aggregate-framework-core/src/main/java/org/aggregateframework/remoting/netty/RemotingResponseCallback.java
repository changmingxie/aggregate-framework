package org.aggregateframework.remoting.netty;

import org.aggregateframework.remoting.protocol.RemotingCommand;

public interface RemotingResponseCallback {

    void callback(RemotingCommand response);
}
