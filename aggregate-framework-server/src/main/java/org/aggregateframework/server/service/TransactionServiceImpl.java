package org.aggregateframework.server.service;

import org.aggregateframework.AggServer;
import org.aggregateframework.alert.ResponseCodeEnum;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.dto.TransactionDetailRequestDto;
import org.aggregateframework.dashboard.dto.TransactionStoreDto;
import org.aggregateframework.dashboard.service.impl.BaseTransactionServiceImpl;
import org.aggregateframework.storage.TransactionStorage;
import org.aggregateframework.storage.TransactionStore;
import org.aggregateframework.xid.TransactionXid;
import org.aggregateframework.xid.Xid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author huabao.fang
 * @Date 2022/5/24 23:17
 **/
@Service
public class TransactionServiceImpl extends BaseTransactionServiceImpl {

    @Autowired
    private AggServer aggServer;

    @Override
    public TransactionStorage getTransactionStorage() {
        return aggServer.getTransactionStorage();
    }

    @Override
    public ResponseDto<TransactionStoreDto> detail(TransactionDetailRequestDto requestDto) {
        String domain = requestDto.getDomain();
        Xid xid = new TransactionXid(requestDto.getXidString());
        TransactionStore transactionStore = getTransactionStorage().findByXid(domain, xid);
        if (transactionStore == null) {
            transactionStore = getTransactionStorage().findMarkDeletedByXid(domain, xid);
        }
        if (transactionStore == null) {
            return ResponseDto.returnFail(ResponseCodeEnum.TRANSACTION_DETAIL_NOT_EXIST);
        }
        TransactionStoreDto transactionStoreDto = toTransactionStoreDto(transactionStore);
        if (!isJSONString(transactionStoreDto.getContent())) {
            byte[] visualizedContent = null;
            try {
                visualizedContent = aggServer.getRecoveryExecutor().transactionVisualize(domain, transactionStore.getContent());
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.length() > 100) {
                    errorMessage = errorMessage.substring(0, 100).concat("...");
                }
                return ResponseDto.returnFail(ResponseCodeEnum.TRANSACTION_CONTENT_VISUALIZE_ERROR_WITH_MESSAGE, errorMessage);
            }
            transactionStoreDto.setContent(new String(visualizedContent));
        }
        return ResponseDto.returnSuccess(transactionStoreDto);
    }

}
