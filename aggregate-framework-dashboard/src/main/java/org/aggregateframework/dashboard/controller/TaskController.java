package org.aggregateframework.dashboard.controller;


import org.aggregateframework.alert.ResponseCodeEnum;
import org.aggregateframework.dashboard.dto.ModifyCronDto;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.dto.TaskDto;
import org.aggregateframework.dashboard.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author huabao.fang
 * @Date 2022/5/30 14:25
 **/
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("/all")
    @ResponseBody
    public ResponseDto<List<TaskDto>> all() {
        return taskService.all();
    }

    @RequestMapping("/pause/{domain}")
    @ResponseBody
    public ResponseDto<Void> pause(@PathVariable("domain") String domain) {
        return taskService.pause(domain);
    }

    @RequestMapping("/resume/{domain}")
    @ResponseBody
    public ResponseDto<Void> resume(@PathVariable("domain") String domain) {
        return taskService.resume(domain);
    }

    @RequestMapping("/delete/{domain}")
    @ResponseBody
    public ResponseDto<Void> delete(@PathVariable("domain") String domain) {
        return ResponseDto.returnFail(ResponseCodeEnum.TASK_OPERATE_NOT_SUPPORT);
    }

    @RequestMapping("/modifyCron")
    @ResponseBody
    public ResponseDto<Void> modifyCron(@RequestBody ModifyCronDto requestDto) {
        return taskService.modifyCron(requestDto);
    }

}
