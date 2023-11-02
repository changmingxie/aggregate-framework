package org.aggregateframework.exception;

import java.lang.reflect.InvocationTargetException;

/**
 * @author changming.xie
 */
public class SystemException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -1168466835428095456L;

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable e) {
        super(e);
    }

    public SystemException(String message, Throwable e) {
        super(message, e);
    }

    public Throwable getOriginThrowable() {
        Throwable t = getCause();
        if (t == null) {
            return null;
        }

        if (t instanceof InvocationTargetException) {
            return t.getCause();
        }

        return t;
    }
}
