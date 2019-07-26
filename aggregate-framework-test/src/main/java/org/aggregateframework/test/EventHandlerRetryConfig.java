package org.aggregateframework.test;

class EventHandlerRetryConfig {
    // 是否打印调试信息
    boolean showDebugMessage;

    // 成功前模拟失败多少次
    int failCount;

    // 事件的唯一Key
    String eventUniqueKey;

    // 模拟失败时抛出的异常
    Class<? extends Exception> throwException;

    public boolean isShowDebugMessage() {
        return showDebugMessage;
    }

    public void setShowDebugMessage(boolean showDebugMessage) {
        this.showDebugMessage = showDebugMessage;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public String getEventUniqueKey() {
        return eventUniqueKey;
    }

    public void setEventUniqueKey(String eventUniqueKey) {
        this.eventUniqueKey = eventUniqueKey;
    }

    public Class<? extends Exception> getThrowException() {
        return throwException;
    }

    public void setThrowException(Class<? extends Exception> throwException) {
        this.throwException = throwException;
    }
}
