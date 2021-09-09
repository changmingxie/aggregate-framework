package org.aggregateframework.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public class SessionFactoryHelper {

    static final Logger logger = LoggerFactory.getLogger(SessionFactoryHelper.class.getSimpleName());

    public static SessionFactoryHelper INSTANCE = new SessionFactoryHelper();

    private static final ThreadLocal<Deque<SessionFactory>> CURRENT = new ThreadLocal<Deque<SessionFactory>>();


    public void registerNewClientSession() {
        startSessionFactoryIfAbsent();
        CURRENT.get().peek().registerNewClientSession();

        logMessage("registerNewClientSession");
    }

    public boolean registerClientSessionIfAbsent() {
        startSessionFactoryIfAbsent();
        boolean result = CURRENT.get().peek().registerClientSessionIfAbsent();

        logMessage("registerClientSessionIfAbsent");

        return result;
    }

    public void startNewSessionFactory() {
        if (CURRENT.get() == null) {
            CURRENT.set(new ArrayDeque<>());
        }

        CURRENT.get().push(new LocalSessionFactory());

        logMessage("startNewSessionFactory");
    }

    public void startSessionFactoryIfAbsent() {
        if (CURRENT.get() == null || CURRENT.get().peek() == null) {
            startNewSessionFactory();
        }
    }


    public ClientSession requireClientSession() {
        return CURRENT.get().peek().requireClientSession();
    }

    public void closeClientSession() {
        CURRENT.get().peek().closeClientSession();

        logMessage("closeClientSession");

        if (CURRENT.get().size() == 1 && !CURRENT.get().peek().hasClientSessions()) {
            CURRENT.set(null);

            logger.debug("ThreadId" + Thread.currentThread() + ",closeClientSession and set ThreadLocal SessionFactoryHelper as null");
        }

    }

    public void closeSessionFactory() {
        CURRENT.get().pop();

        logMessage("closeSessionFactory");

        if (CURRENT.get().size() == 0) {
            CURRENT.set(null);

            logger.debug("ThreadId" + Thread.currentThread() + ",closeSessionFactory and set ThreadLocal SessionFactoryHelper as null");
        }
    }


    private void logMessage(String logPoint) {
        StringBuilder message = new StringBuilder();
        message.append("###").append(logPoint).append("###\r\n");
        if (CURRENT.get().size() > 0) {
            for (SessionFactory sessionFactory : CURRENT.get()) {
                message.append(String.format("ThreadId:%s, sessionFactory:%s,clientSessionCount:%d\r\n", Thread.currentThread(), sessionFactory, sessionFactory.clientSessionCount()));
            }
        } else {
            message.append("ThreadId" + Thread.currentThread() + ",current no SessionFactory!");
        }
        logger.debug(message.toString());
    }

}
