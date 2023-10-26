package org.aggregateframework.dashboard.service.impl.aggserver;


import com.netflix.loadbalancer.Server;
import org.aggregateframework.alert.ResponseCodeEnum;
import org.aggregateframework.dashboard.constants.DashboardConstant;
import org.aggregateframework.dashboard.dto.*;
import org.aggregateframework.dashboard.service.TransactionService;
import org.aggregateframework.dashboard.service.condition.AggServerStorageCondition;
import org.aggregateframework.storage.TransactionStorage;
import org.aggregateframework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @Author huabao.fang
 * @Date 2022/5/30 14:19
 **/
@Conditional(AggServerStorageCondition.class)
@Service
public class AggServerTransactionServiceImpl implements TransactionService {

    private Logger logger = LoggerFactory.getLogger(AggServerTransactionServiceImpl.class);

    private static final String REQUEST_METHOD_TRANSACTION_DETAIL = "transaction/detail";

    @Autowired
    private AggServerFeignClient aggServerFeignClient;

    //可以获取注册中心上的服务列表
    @Autowired
    private SpringClientFactory springClientFactory;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseDto<TransactionPageDto> list(TransactionPageRequestDto requestDto) {
        return aggServerFeignClient.transactionList(requestDto);
    }

    @Override
    public ResponseDto<TransactionStoreDto> detail(TransactionDetailRequestDto requestDto) {
        List<Server> servers = springClientFactory.getLoadBalancer(DashboardConstant.AGG_SERVER_GROUP).getReachableServers();
        if (CollectionUtils.isEmpty(servers)) {
            return ResponseDto.returnFail(ResponseCodeEnum.TRANSACTION_DETAIL_NO_INSTANCES);
        }

        String errorMessage = "";
        String errorCode = "";
        for (Server server : servers) {
            String detailRequestUrl = "http://"
                    .concat(server.getHostPort())
                    .concat("/")
                    .concat(DashboardConstant.AGG_SERVER_GROUP)
                    .concat("/")
                    .concat(REQUEST_METHOD_TRANSACTION_DETAIL);
            try {
                ResponseDto<TransactionStoreDto> responseDto = restTemplate.postForObject(detailRequestUrl, requestDto, ResponseDto.class);
                if (responseDto.isSuccess()) {
                    return responseDto;
                }
                errorMessage = responseDto.getMessage();
                errorCode = responseDto.getCode();
            } catch (Exception e) {
                logger.warn("request detailRequestUrl:{} failed!", detailRequestUrl, e);
            }

        }
        if (StringUtils.isEmpty(errorMessage)) {
            return ResponseDto.returnFail(ResponseCodeEnum.TRANSACTION_CONTENT_VISUALIZE_ERROR);
        } else {
            return ResponseDto.returnFail(errorCode, errorMessage);
        }

    }

    @Override
    public ResponseDto<Void> reset(TransactionOperateRequestDto requestDto) {
        return aggServerFeignClient.transactionReset(requestDto);
    }

    @Override
    public ResponseDto<Void> markDeleted(TransactionOperateRequestDto requestDto) {
        return aggServerFeignClient.transactionMarkDeleted(requestDto);
    }

    @Override
    public ResponseDto<Void> restore(TransactionOperateRequestDto requestDto) {
        return aggServerFeignClient.transactionRestore(requestDto);
    }

    @Override
    public ResponseDto<Void> delete(TransactionOperateRequestDto requestDto) {
        return aggServerFeignClient.transactionDelete(requestDto);
    }

    @Override
    public TransactionStorage getTransactionStorage() {
        return null;
    }
}
