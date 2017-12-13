package org.aggregateframework.session;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午5:12
 */
public class LocalSessionFactory implements SessionFactory {

    public static SessionFactory INSTANCE = new LocalSessionFactory();

    private static final ThreadLocal<Deque<SessionEntry>> CURRENT = new ThreadLocal<Deque<SessionEntry>>();

    @Override
    public boolean registerClientSession(boolean requireNew) {

        if (requireNew) {

            if (CURRENT.get() == null) {
                CURRENT.set(new ArrayDeque<SessionEntry>());
            }

            CURRENT.get().push(new SessionEntry(new UnitOfWork()));
            return true;
        } else {

            if (CURRENT.get() == null) {
                CURRENT.set(new ArrayDeque<SessionEntry>());
                CURRENT.get().push(new SessionEntry(new UnitOfWork()));
                return true;
            } else if (CURRENT.get().peek() == null) {
                CURRENT.get().push(new SessionEntry(new UnitOfWork()));
                return true;
            }

            return false;
        }
    }

    @Override
    public ClientSession requireClientSession() {
        return CURRENT.get().peek().getClientSession();
    }

    @Override
    public void closeClientSession() {
        CURRENT.get().pop();
    }

    class SessionEntry {

        private ClientSession clientSession;

        public SessionEntry(ClientSession clientSession) {
            this.clientSession = clientSession;
        }

        ClientSession getClientSession() {
            return clientSession;
        }
    }
}
