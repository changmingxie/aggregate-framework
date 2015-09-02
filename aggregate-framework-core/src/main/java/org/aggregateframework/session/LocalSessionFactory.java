package org.aggregateframework.session;

import java.util.Deque;
import java.util.LinkedList;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午5:12
 */
public class LocalSessionFactory implements SessionFactory {

    public static SessionFactory INSTANCE = new LocalSessionFactory();

    private static final ThreadLocal<Deque<SessionEntry>> CURRENT = new ThreadLocal<Deque<SessionEntry>>();

    private static boolean isEmpty() {
        Deque<SessionEntry> session = CURRENT.get();
        return session == null || session.isEmpty();
    }

    @Override
    public ClientSession registerClientSession(ClientSession clientClientSession) {

        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<SessionEntry>());
        }

        CURRENT.get().push(new SessionEntry(clientClientSession));
        return requireClientSession();
    }

    @Override
    public ClientSession requireClientSession() {
        if (isEmpty()) {
            if (CURRENT.get() == null) {
                CURRENT.set(new LinkedList<SessionEntry>());
            }

            CURRENT.get().push(new SessionEntry(new NoClientSession()));
        }
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
