package org.aggregateframework.ignite.transaction;

import org.aggregateframework.session.LocalSessionFactory;
import org.aggregateframework.session.SessionFactory;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Created by changming.xie on 11/4/16.
 */
public class SessionIgniteTransactionManager extends SpringTransactionManager {

    private static final String TRANSACTIONAL_TEST_DEFINITION_CLASS_NAME = "1org.springframework.test.context.transaction.TransactionalTestExecutionListener$1";
    private static final long serialVersionUID = 1820247192845134456L;

    private SessionFactory sessionFactory = LocalSessionFactory.INSTANCE;

    public SessionIgniteTransactionManager() {

    }

    public SessionIgniteTransactionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        if (this.getResourceFactory() == null) {

            if (Ignition.state(this.getGridName()) == IgniteState.STARTED) {
                this.setConfiguration(null);
                this.setConfigurationPath(null);
                super.afterPropertiesSet();
            } else {
                super.afterPropertiesSet();
            }
        }
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

//        if (definition.getClass().getName().equals(TRANSACTIONAL_TEST_DEFINITION_CLASS_NAME)) {
//            sessionFactory.registerClientSession(new NoClientSession());
//        } else {
//            sessionFactory.registerClientSession(new UnitOfWork());
//        }
        sessionFactory.registerClientSession();
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        sessionFactory.requireClientSession().commit();
        super.doCommit(status);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {

        sessionFactory.requireClientSession().rollback();
        super.doRollback(status);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        try {
            super.doCleanupAfterCompletion(transaction);
            sessionFactory.requireClientSession().postHandle();
        } finally {
            sessionFactory.closeClientSession();
        }
    }
}
