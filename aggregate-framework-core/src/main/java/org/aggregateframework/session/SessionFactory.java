package org.aggregateframework.session;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午4:58
 */
public interface SessionFactory {

    boolean registerClientSession(boolean requireNew);

    ClientSession requireClientSession();

    void closeClientSession();
}
