package org.aggregateframework.session;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午5:12
 */
public class LocalSessionFactory implements SessionFactory {

    private final Deque<SessionEntry> sessionEntries = new ArrayDeque<SessionEntry>();

    @Override
    public void registerNewClientSession() {
        sessionEntries.push(new SessionEntry(new UnitOfWork()));
    }

    @Override
    public boolean registerClientSessionIfAbsent() {
        if (sessionEntries.peek() == null) {
            sessionEntries.push(new SessionEntry(new UnitOfWork()));
            return true;
        }
        return false;
    }

    @Override
    public boolean hasClientSessions() {
        return sessionEntries.size() > 0;
    }

    @Override
    public int clientSessionCount() {
        return sessionEntries.size();
    }

    @Override
    public ClientSession requireClientSession() {
        return sessionEntries.peek().getClientSession();
    }

    @Override
    public void closeClientSession() {
        sessionEntries.pop();
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
