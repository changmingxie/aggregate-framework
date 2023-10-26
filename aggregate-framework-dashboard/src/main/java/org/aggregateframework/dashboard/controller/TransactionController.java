package org.aggregateframework.dashboard.controller;


import org.aggregateframework.dashboard.dto.*;
import org.aggregateframework.dashboard.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author huabao.fang
 * @Date 2022/5/30 14:25
 **/
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseDto<TransactionPageDto> list(@RequestBody TransactionPageRequestDto requestDto) {
        return transactionService.list(requestDto);
    }

    @RequestMapping("/detail")
    @ResponseBody
    public ResponseDto<TransactionStoreDto> detail(@RequestBody TransactionDetailRequestDto requestDto) {
        return transactionService.detail(requestDto);
    }

    @RequestMapping("/reset")
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
