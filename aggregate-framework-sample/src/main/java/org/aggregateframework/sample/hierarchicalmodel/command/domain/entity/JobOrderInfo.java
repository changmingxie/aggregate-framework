package org.aggregateframework.sample.hierarchicalmodel.command.domain.entity;

/**
 * Created by changming.xie on 3/30/16.
 */
public class JobOrderInfo extends OrderInfo {

    private String jobInfo;

    public JobOrderInfo() {
        this.setDtype("JobOrderInfo");
    }

    public String getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(String jobInfo) {
        this.jobInfo = jobInfo;
    }
}
