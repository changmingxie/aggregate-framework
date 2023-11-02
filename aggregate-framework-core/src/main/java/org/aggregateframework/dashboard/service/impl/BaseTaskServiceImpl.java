package org.aggregateframework.dashboard.service.impl;


import org.aggregateframework.alert.ResponseCodeEnum;
import org.aggregateframework.dashboard.dto.ModifyCronDto;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.dto.TaskDto;
import org.aggregateframework.dashboard.exception.TransactionException;
import org.aggregateframework.dashboard.service.TaskService;

import java.util.List;

/**
 * @Author huabao.fang
 * @Date 2022/6/9 17:03
 **/
public class BaseTaskServiceImpl implements TaskService {

    @Override
    public ResponseDto<List<TaskDto>> all() {
        throw new TransactionException(ResponseCodeEnum.TASK_OPERATE_NOT_SUPPORT);
    }

    @Override
    public ResponseDto<Void> pause(String domain) {
        throw new TransactionException(ResponseCodeEnum.TASK_OPERATE_NOT_SUPPORT);
    }

    @Override
    public ResponseDto<Void> resume(String domain) {
        throw new TransactionException(ResponseCodeEnum.TASK_OPERATE_NOT_SUPPORT);
    }

    @Override
    public ResponseDto<Void> modifyCron(ModifyCronDto requestDto) {
        throw new TransactionException(ResponseCodeEnum.TASK_OPERATE_NOT_SUPPORT);
    }

    @Override
    public ResponseDto<Void> delete(String domain) {
        throw new TransactionException(ResponseCodeEnum.TASK_OPERATE_NOT_SUPPORT);
    }

}
