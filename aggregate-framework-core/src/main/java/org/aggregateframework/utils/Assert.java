package org.aggregateframework.utils;

import org.aggregateframework.exception.SystemException;

/**
 * @author changming.xie
 */
public class Assert {
    public static void notNull(Object object, String message) {

        if (object == null) {
            throw new SystemException(message);
        }
    }
}
