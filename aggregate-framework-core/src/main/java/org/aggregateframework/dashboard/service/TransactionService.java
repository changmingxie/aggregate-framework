package org.aggregateframework.dashboard.service;

import org.aggregateframework.dashboard.dto.*;

/**
 * @Author huabao.fang
 * @Date 2022/5/30 10:08
 **/
public interface TransactionService extends TransactionStorageable {

    /**
     * 事件分页查询
     *
     * @param requestDto
     * @return
     */
    ResponseDto<TransactionPageDto> list(TransactionPageRequestDto requestDto);

    ResponseDto<TransactionStoreDto> detail(TransactionDetailRequestDto requestDto);

    ResponseDto<Void> reset(TransactionOperateRequestDto requestDto);

    ResponseDto<Void> markDeleted(TransactionOperateRequestDto requestDto);

    ResponseDto<Void> restore(TransactionOperateRequestDto requestDto);

    ResponseDto<Void> delete(TransactionOperateRequestDto requestDto);

}
