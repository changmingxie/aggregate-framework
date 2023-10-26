package org.aggregateframework.recovery;

import org.aggregateframework.AggService;
import org.aggregateframework.constants.MixAll;
import org.aggregateframework.support.FactoryBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class QuartzRecoveryTask implements Job {

    static final Logger logger = LoggerFactory.getLogger(QuartzRecoveryTask.class.getSimpleName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String domain = context.getJobDetail().getJobDataMap().getString(MixAll.DOMAIN);
        logger.info("start recovery {}", domain);
        FactoryBuilder.factoryOf(AggService.class).getInstance().getTransactionStoreRecovery().startRecover(domain);
    }
}
