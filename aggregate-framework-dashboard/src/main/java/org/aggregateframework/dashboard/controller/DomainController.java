package org.aggregateframework.dashboard.controller;


import org.aggregateframework.dashboard.config.DashboardProperties;
import org.aggregateframework.dashboard.dto.DomainStoreDto;
import org.aggregateframework.dashboard.dto.DomainStoreRequestDto;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.enums.ConnectionMode;
import org.aggregateframework.dashboard.service.DomainService;
import org.aggregateframework.dashboard.service.TaskService;
import org.aggregateframework.utils.AlertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author huabao.fang
 * @Date 2022/5/24 12:41
 **/
@RestController
@RequestMapping("/api/domain")
public class DomainController {

    @Autowired
    private DomainService domainService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private DashboardProperties dashboardProperties;

    @GetMapping("/allKeys")
    @ResponseBody
    public ResponseDto<List<String>> allKeys() {
        return rebuildAllDomainKeys(domainService.getAllDomainKeys());
    }

    @RequestMapping("/all")
    @ResponseBody
    public ResponseDto<List<DomainStoreDto>> all() {
        return rebuildAllDomains(domainService.getAllDomains());
    }

    @RequestMapping("/create")
    @ResponseBody
    public ResponseDto<Void> create(@RequestBody DomainStoreRequestDto requestDto) {
        return domainService.create(requestDto);
    }

    @RequestMapping("/modify")
    @ResponseBody
    public ResponseDto<Void> modify(@RequestBody DomainStoreRequestDto requestDto) {
        return domainService.modify(requestDto);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public ResponseDto<Void> delete(@RequestBody DomainStoreRequestDto requestDto) {
        // server模式时，删除domain前先删除任务
        if (ConnectionMode.SERVER.equals(dashboardProperties.getConnectionMode())) {
            ResponseDto<Void> taskDeleteResponseDto = taskService.delete(requestDto.getDomain());
            if (!taskDeleteResponseDto.isSuccess()) {
                return taskDeleteResponseDto;
            }
        }
        return domainService.delete(requestDto);
    }

    @RequestMapping("/alertTest")
    @ResponseBody
    public ResponseDto<Void> alertTest(@RequestBody DomainStoreRequestDto requestDto) {
        AlertUtils.doDingAlert(requestDto.getDingRobotUrl(), requestDto.getPhoneNumbers(), "AGG告警:domain[" + requestDto.getDomain() + "]测试");
        return ResponseDto.returnSuccess();
    }

    private ResponseDto<List<String>> rebuildAllDomainKeys(ResponseDto<List<String>> allDomainKeysResponseDto) {
        List<String> list = allDomainKeysResponseDto.getData();
        return ResponseDto.returnSuccess(list);
    }

    private ResponseDto<List<DomainStoreDto>> rebuildAllDomains(ResponseDto<List<DomainStoreDto>> allDomainsResponseDto) {
        List<DomainStoreDto> list = allDomainsResponseDto.getData();
        return ResponseDto.returnSuccess(list);
    }

}
