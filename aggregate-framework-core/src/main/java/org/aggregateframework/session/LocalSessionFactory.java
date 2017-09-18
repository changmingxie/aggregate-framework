package org.aggregateframework.session;

/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午5:12
 */
public class LocalSessionFactory implements SessionFactory {

    public static SessionFactory INSTANCE = new LocalSessionFactory();

    private static final ThreadLocal<SessionEntry> CURRENT = new ThreadLocal<SessionEntry>();

    private static boolean isEmpty() {

        SessionEntry session = CURRENT.get();
        return session == null;
    }

    @Override
    public boolean registerClientSession() {

        if (CURRENT.get() == null) {
            CURRENT.set(new SessionEntry(new UnitOfWork()));

            return true;
        }

        return false;
    }

    @Override
    public ClientSession requireClientSession() {
        return CURRENT.get().getClientSession();
    }

    @Override
    public void closeClientSession() {
        CURRENT.set(null);
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
