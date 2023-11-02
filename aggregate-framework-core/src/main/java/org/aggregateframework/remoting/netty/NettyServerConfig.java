package org.aggregateframework.remoting.netty;

public interface NettyServerConfig extends NettyConfig {

    int getListenPort();

    int getChannelIdleTimeoutSeconds();

    int getFlowMonitorPrintIntervalMinutes();
}
