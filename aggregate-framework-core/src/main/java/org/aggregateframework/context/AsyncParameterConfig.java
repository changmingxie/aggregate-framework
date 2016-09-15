package org.aggregateframework.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncParameterConfig {

    public static List<PayloadDisruptorConfig> PAYLOAD_TYPE_DISRUPTOR_CONFIGS = new ArrayList<PayloadDisruptorConfig>();

    public static Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool();
}