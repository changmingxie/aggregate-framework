package org.aggregateframework.spring.datasource;

import org.aggregateframework.session.ClientSession;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

public class SessionTransactionSynchronizationAdapter extends TransactionSynchronizationAdapter {

    ClientSession clientSession;

    public SessionTransactionSynchronizationAdapter(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        if (!readOnly) {
            clientSession.commit();
        }
    }

    @Override
    public void afterCommit() {
        clientSession.flushToL2Cache();
        clientSession.postHandle();
    }

    @Override
    public void afterCompletion(int status) {
        try {
            switch (status) {
                case TransactionSynchronization.STATUS_COMMITTED:
                    //do nothing
                    break;
                case TransactionSynchronization.STATUS_ROLLED_BACK:
                    clientSession.rollback();
                    break;
                case TransactionSynchronization.STATUS_UNKNOWN:
                    //do nothing
                    break;
            }
        }finally {
            clientSession.clear();
        }
    }
}
