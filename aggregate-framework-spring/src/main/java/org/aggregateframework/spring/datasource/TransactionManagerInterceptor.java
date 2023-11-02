package org.aggregateframework.spring.datasource;

import org.aggregateframework.session.SessionFactoryHelper;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

public class TransactionManagerInterceptor implements MethodInterceptor {

    private final static String GET_TRANSACTION_METHOD_NAME = "getTransaction";

    private final static String COMMIT_METHOD_NAME = "commit";

    private final static String ROLLBACK_METHOD_NAME = "rollback";

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        if (method.getName().equals(GET_TRANSACTION_METHOD_NAME)) {

            Object res = invocation.proceed();

            TransactionStatus transactionStatus = (TransactionStatus) res;

            if (transactionStatus.isNewTransaction()) {
                SessionFactoryHelper.INSTANCE.registerNewClientSession();
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new SessionTransactionSynchronizationAdapter(SessionFactoryHelper.INSTANCE.requireClientSession()));
                }
            }

            return res;
        }


        if (method.getName().equals(COMMIT_METHOD_NAME) ||
                method.getName().equals(ROLLBACK_METHOD_NAME)) {

            TransactionStatus transactionStatus = (TransactionStatus) invocation.getArguments()[0];

            if (transactionStatus.isNewTransaction()) {

                try {
                    
                    return invocation.proceed();

                } finally {
                    SessionFactoryHelper.INSTANCE.closeClientSession();
                }
            }
        }

        return invocation.proceed();

    }
}
