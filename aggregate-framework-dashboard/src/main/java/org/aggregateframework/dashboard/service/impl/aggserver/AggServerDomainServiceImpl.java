package org.aggregateframework.dashboard.service.impl.aggserver;


import org.aggregateframework.dashboard.dto.DomainStoreDto;
import org.aggregateframework.dashboard.dto.DomainStoreRequestDto;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.service.DomainService;
import org.aggregateframework.dashboard.service.condition.AggServerStorageCondition;
import org.aggregateframework.storage.TransactionStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author huabao.fang
 * @Date 2022/5/30 14:17
 **/
@Conditional(AggServerStorageCondition.class)
@Service
public class AggServerDomainServiceImpl implements DomainService {

    @Autowired
    private AggServerFeignClient aggServerFeignClient;

    @Override
    public ResponseDto<List<String>> getAllDomainKeys() {
        return aggServerFeignClient.allDomainKeys();
    }

    @Override
    public ResponseDto<List<DomainStoreDto>> getAllDomains() {
        return aggServerFeignClient.all();
    }

    @Override
    public ResponseDto<Void> create(DomainStoreRequestDto requestDto) {
        return aggServerFeignClient.createDomain(requestDto);
    }

    @Override
    public ResponseDto<Void> modify(DomainStoreRequestDto requestDto) {
        return aggServerFeignClient.modifyDomain(requestDto);
    }

    @Override
    public ResponseDto<Void> delete(DomainStoreRequestDto requestDto) {
        return aggServerFeignClient.deleteDomain(requestDto);
    }

    @Override
    public TransactionStorage getTransactionStorage() {
        return null;
    }
}
