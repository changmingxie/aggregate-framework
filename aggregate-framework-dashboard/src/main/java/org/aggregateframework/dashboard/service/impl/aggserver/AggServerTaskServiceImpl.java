package org.aggregateframework.dashboard.service.impl.aggserver;

import org.aggregateframework.AggClient;
import org.aggregateframework.alert.ResponseCodeEnum;
import org.aggregateframework.dashboard.dto.ModifyCronDto;
import org.aggregateframework.dashboard.dto.ResponseDto;
import org.aggregateframework.dashboard.dto.TaskDto;
import org.aggregateframework.dashboard.exception.TransactionException;
import org.aggregateframework.dashboard.service.DomainService;
import org.aggregateframework.dashboard.service.TaskService;
import org.aggregateframework.dashboard.service.condition.AggServerStorageCondition;
import org.aggregateframework.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author huabao.fang
 * @Date 2022/5/30 11:51
 **/
@Conditional(AggServerStorageCondition.class)
@Service
public class AggServerTaskServiceImpl implements TaskService {

    private Logger logger = LoggerFactory.getLogger(AggServerTaskServiceImpl.class.getSimpleName());

    @Autowired
    private AggClient aggClient;

    @Autowired
    private DomainService domainService;

    @Override
    public ResponseDto<List<TaskDto>> all() {
        List<String> domainList = domainService.getAllDomainKeys().getData();
        try {
            return ResponseDto.returnSuccess(buildTaskList(domainList));
        } catch (SchedulerException e) {
            logger.error("task all error", e);
            return ResponseDto.returnFail(ResponseCodeEnum.TASK_OPERATE_ERROR);
        }
    }

    @Override
    public ResponseDto<Void> pause(String domain) {
        try {
            aggClient.getScheduler().getScheduler(domain).pauseJob(selectJobKey(domain));
            logger.info("domain:{} task paused", domain);
        } catch (SchedulerException e) {
            logger.error("pasuse job for domain:{} error", domain, e);
            return ResponseDto.returnFail(ResponseCodeEnum.TASK_OPERATE_ERROR);
        }
        return ResponseDto.returnSuccess();
    }

    @Override
    public ResponseDto<Void> resume(String domain) {
        try {
            aggClient.getScheduler().getScheduler(domain).resumeJob(selectJobKey(domain));
            logger.info("domain:{} task resumed", domain);
        } catch (SchedulerException e) {
            logger.error("resume job for domain:{} error", domain, e);
            return ResponseDto.returnFail(ResponseCodeEnum.TASK_OPERATE_ERROR);
        }
        return ResponseDto.returnSuccess();
    }

    @Override
    public ResponseDto<Void> modifyCron(ModifyCronDto requestDto) {
        try {
            TriggerKey triggerKey = selectTriggerKey(requestDto.getDomain());
            CronTrigger currentCronTrigger = (CronTrigger) aggClient.getScheduler().getScheduler(requestDto.getDomain()).getTrigger(triggerKey);
            String currentCron = currentCronTrigger.getCronExpression();
            if (StringUtils.isNotEmpty(requestDto.getCronExpression())
                    && !requestDto.getCronExpression().equals(currentCron)) {
                CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey.getName())
                        .withSchedule(CronScheduleBuilder.cronSchedule(requestDto.getCronExpression())
                                .withMisfireHandlingInstructionDoNothing()).build();
                aggClient.getScheduler().getScheduler(requestDto.getDomain()).rescheduleJob(triggerKey, cronTrigger);
            }
            logger.info("domain:{} update cron from {} to {} success", requestDto.getDomain(), currentCron, requestDto.getCronExpression());
        } catch (SchedulerException e) {
            logger.error("modifyCron error", e);
            return ResponseDto.returnFail(ResponseCodeEnum.TASK_MODIFY_CRON_ERROR);
        }

        return ResponseDto.returnSuccess();
    }

    @Override
    public ResponseDto<Void> delete(String domain) {
        try {
            JobKey jobKey = selectJobKey(domain);
            aggClient.getScheduler().getScheduler(domain).deleteJob(jobKey);
            logger.info("domain:{} task deleted", domain);
        } catch (SchedulerException e) {
            logger.error("delete job for domain:{} error", domain, e);
            return ResponseDto.returnFail(ResponseCodeEnum.TASK_MODIFY_CRON_ERROR);
        }
        return ResponseDto.returnSuccess();
    }

    private JobKey selectJobKey(String domain) throws SchedulerException {
        aggClient.getScheduler().registerScheduleIfNotPresent(domain);
        Set<JobKey> jobKeySet = aggClient.getScheduler().getScheduler(domain).getJobKeys(GroupMatcher.anyGroup());
        if (CollectionUtils.isEmpty(jobKeySet)) {
            throw new TransactionException(ResponseCodeEnum.TASK_STATUS_ERROR);
        }
        return jobKeySet.iterator().next();
    }

    private TriggerKey selectTriggerKey(String domain) throws SchedulerException {
        Set<TriggerKey> triggerKeySet = aggClient.getScheduler().getScheduler(domain).getTriggerKeys(GroupMatcher.anyGroup());
        if (CollectionUtils.isEmpty(triggerKeySet)) {
            throw new TransactionException(ResponseCodeEnum.TASK_STATUS_ERROR);
        }
        return triggerKeySet.iterator().next();
    }

    private List<TaskDto> buildTaskList(List<String> domainList) throws SchedulerException {
        List<TaskDto> taskDtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(domainList)) {
            return taskDtoList;
        }
        for (String domain : domainList) {
            Scheduler scheduler = aggClient.getScheduler().registerScheduleIfNotPresent(domain);
            Set<JobKey> jobKeySet = aggClient.getScheduler().getScheduler(domain).getJobKeys(GroupMatcher.anyGroup());
            if (CollectionUtils.isEmpty(jobKeySet)) {
                continue;
            }
            JobKey jobKey = jobKeySet.iterator().next();
            Set<TriggerKey> triggerKeySet = aggClient.getScheduler().getScheduler(domain).getTriggerKeys(GroupMatcher.anyGroup());
            if (CollectionUtils.isEmpty(triggerKeySet)) {
                continue;
            }
            TriggerKey triggerKey = triggerKeySet.iterator().next();
            CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (cronTrigger == null) {
                continue;
            }
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            taskDtoList.add(new TaskDto(scheduler.getSchedulerName(), domain, jobKey.getGroup(), jobKey.getName(), triggerState.name(), cronTrigger.getCronExpression()));
        }
        return taskDtoList;
    }
}
