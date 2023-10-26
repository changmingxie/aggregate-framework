package org.aggregateframework.threadcontext;

import java.util.ServiceLoader;

public class ThreadContextSynchronizationManager {

    public static String THREAD_CONTEXT_SYNCHRONIZATION_KEY = "THREAD_CONTEXT_SYNCHRONIZATION_KEY";


    private static volatile ThreadContextSynchronization threadContextSynchronization = new EmptyThreadContextSynchronization();

    static {
        load();
    }

    private String threadContext = null;
    private String currentThreadContext = null;

    public ThreadContextSynchronizationManager(String threadContext) {
        this.threadContext = threadContext;
    }

    public static void load() {
        ServiceLoader<ThreadContextSynchronization> loadedThreadContextSynchronizations = ServiceLoader.load(ThreadContextSynchronization.class);

        for (ThreadContextSynchronization synchronization : loadedThreadContextSynchronizations) {
            threadContextSynchronization = synchronization;
            break;
        }
    }

    public static ThreadContextSynchronization getThreadContextSynchronization() {
        return threadContextSynchronization;
    }

    public void executeWithBindThreadContext(Runnable runnable) {
        bindThreadContext();
        try {
            runnable.run();
        } finally {
            unbindThreadContext();
        }
    }

    protected void bindThreadContext() {
        currentThreadContext = threadContextSynchronization.getCurrentThreadContext();

        threadContextSynchronization.setThreadContext(threadContext);
    }

    protected void unbindThreadContext() {
        threadContextSynchronization.clear();

        threadContextSynchronization.setThreadContext(currentThreadContext);
    }
}
