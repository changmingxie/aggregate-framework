package org.aggregateframework.spring.datasource;


import org.aggregateframework.session.SessionFactoryHelper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;


/**
 * User: changming.xie
 * Date: 14-7-29
 * Time: 下午1:15
 * deprecated, use DataSourceTransactionManager directly. TransactionManagerAutoProxyCreator will proxy the PlatformTransactionManager and interceptor the methods.
 *
 */
@Deprecated
public class SessionDataSourceTransactionManager extends DataSourceTransactionManager {

    private static final String TRANSACTIONAL_TEST_DEFINITION_CLASS_NAME = "1org.springframework.test.context.transaction.TransactionalTestExecutionListener$1";
    private static final long serialVersionUID = -6436694485382392463L;

    private static int TRANSACTION_STATUS_UNKNOWN = 0;
    private static int TRANSACTION_STATUS_COMMIT = 1;
    private static int TRANSACTION_STATUS_ROLLBACK = 2;

    private SessionFactoryHelper sessionFactoryHelper = SessionFactoryHelper.INSTANCE;


    private static final ThreadLocal<Integer> TRANSACTION_STATUS = new ThreadLocal<Integer>();

    public SessionDataSourceTransactionManager() {

    }


    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

//        if (definition.getClass().getName().equals(TRANSACTIONAL_TEST_DEFINITION_CLASS_NAME)) {
//            sessionFactory.registerClientSession(new NoClientSession());
//        } else {
//            sessionFactory.registerClientSession(new UnitOfWork());
//        }

        sessionFactoryHelper.registerNewClientSession();
        TRANSACTION_STATUS.set(TRANSACTION_STATUS_UNKNOWN);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        sessionFactoryHelper.requireClientSession().commit();
        super.doCommit(status);
        TRANSACTION_STATUS.set(TRANSACTION_STATUS_COMMIT);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        TRANSACTION_STATUS.set(TRANSACTION_STATUS_ROLLBACK);
        sessionFactoryHelper.requireClientSession().rollback();
        super.doRollback(status);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {

        try {

            super.doCleanupAfterCompletion(transaction);

            if (TRANSACTION_STATUS.get() == TRANSACTION_STATUS_COMMIT) {
                sessionFactoryHelper.requireClientSession().flushToL2Cache();

                sessionFactoryHelper.requireClientSession().postHandle();

            } else if (TRANSACTION_STATUS.get() == TRANSACTION_STATUS_ROLLBACK) {
                //the rollback is called onRollback method, needn't call again.
//                sessionFactory.requireClientSession().rollback();
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            sessionFactoryHelper.closeClientSession();
            TRANSACTION_STATUS.set(TRANSACTION_STATUS_UNKNOWN);
        }
    }
}
