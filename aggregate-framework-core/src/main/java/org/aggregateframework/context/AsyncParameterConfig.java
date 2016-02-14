package org.aggregateframework.context;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by changmingxie on 12/2/15.
 */
public class AsyncParameterConfig {

    public static volatile Executor EXECUTOR = Executors.newCachedThreadPool();

    public static volatile int DISRUPTOR_RING_BUFFER_SIZE = 1024 * 1024;

    public static volatile int ASYNC_EVENT_HANDLER_WORK_POOL_SIZE = 10;


    public static volatile Executor RETRY_EXECUTOR = Executors.newCachedThreadPool();

    public static volatile int DISRUPTOR_RETRY_RING_BUFFER_SIZE = 1024 * 1024;

    public static volatile int RETRY_EVENT_HANDLER_WORK_POOL_SIZE = 10;
}