package org.aggregateframework.transaction;

/**
 * Created by changming.xie on 7/21/16.
 */
public class CancellingException extends RuntimeException {


    private static final long serialVersionUID = -1250722688869888254L;

    public CancellingException(Throwable cause) {
        super(cause);
    }
}
