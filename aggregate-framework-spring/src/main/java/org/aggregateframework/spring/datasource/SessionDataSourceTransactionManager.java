package org.aggregateframework.spring.datasource;


import org.aggregateframework.session.LocalSessionFactory;
import org.aggregateframework.session.SessionFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;


/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午1:15
 */
public class SessionDataSourceTransactionManager extends DataSourceTransactionManager {

    private static final String TRANSACTIONAL_TEST_DEFINITION_CLASS_NAME = "1org.springframework.test.context.transaction.TransactionalTestExecutionListener$1";
    private static final long serialVersionUID = -6436694485382392463L;
    private SessionFactory sessionFactory = LocalSessionFactory.INSTANCE;

    public SessionDataSourceTransactionManager() {

    }

    public SessionDataSourceTransactionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected Object doGetTransaction() {
        Object txObject = super.doGetTransaction();
        return txObject;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

//        if (definition.getClass().getName().equals(TRANSACTIONAL_TEST_DEFINITION_CLASS_NAME)) {
//            sessionFactory.registerClientSession(new NoClientSession());
//        } else {
//            sessionFactory.registerClientSession(new UnitOfWork());
//        }
        sessionFactory.registerClientSession(true);
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
            sessionFactory.requireClientSession().flushToL2Cache();
            sessionFactory.requireClientSession().postHandle();
        } finally {
            sessionFactory.closeClientSession();
        }
    }
}
