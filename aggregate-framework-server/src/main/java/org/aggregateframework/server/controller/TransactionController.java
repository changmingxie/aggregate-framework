package org.aggregateframework.server.controller;

import org.aggregateframework.dashboard.dto.*;
import org.aggregateframework.server.service.TransactionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author huabao.fang
 * @Date 2022/5/19 12:19
 * 事件管理
 **/
@RestController
@RequestMapping("transaction")
public class TransactionController {

    @Autowired
    private TransactionServiceImpl transactionService;

    /**
     * 事件分页查询
     *
     * @param requestDto
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseDto<TransactionPageDto> list(@RequestBody TransactionPageRequestDto requestDto) {
        return transactionService.list(requestDto);
    }

    /**
     * 事件详情
     *
     * @param requestDto
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public ResponseDto<TransactionStoreDto> detail(@RequestBody TransactionDetailRequestDto requestDto) {
        return transactionService.detail(requestDto);
    }

    @RequestMapping("/reset")
    @ResponseBody
    public ResponseDto<Void> reset(@RequestBody TransactionOperateRequestDto requestDto) {
        return transactionService.reset(requestDto);
    }

    @RequestMapping("/markDeleted")
    @ResponseBody
    public ResponseDto<Void> markDeleted(@RequestBody TransactionOperateRequestDto requestDto) {
        return transactionService.markDeleted(requestDto);
    }

    @RequestMapping("/restore")
    @ResponseBody
    public ResponseDto<Void> restore(@RequestBody TransactionOperateRequestDto requestDto) {
        return transactionService.restore(requestDto);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public ResponseDto<Void> delete(@RequestBody TransactionOperateRequestDto requestDto) {
        return transactionService.delete(requestDto);
    }


}
