package org.aggregateframework.exception;

/**
 * @author changming.xie
 */
public class NoDaoDefinitionException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 9005161277240779449L;


    public NoDaoDefinitionException() {
        super();
    }

    public NoDaoDefinitionException(String message) {
        super(message);
    }
}
