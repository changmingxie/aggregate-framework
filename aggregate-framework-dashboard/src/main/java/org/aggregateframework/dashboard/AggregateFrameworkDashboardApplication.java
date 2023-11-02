package org.aggregateframework.dashboard;

import org.aggregateframework.dashboard.constants.DashboardConstant;
import org.aggregateframework.ribbon.AggFeignClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created by Lee on 2020/4/8 12:56.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
@RibbonClients(value = {@RibbonClient(name = DashboardConstant.AGG_SERVER_GROUP, configuration = AggFeignClientConfig.class)})
public class AggregateFrameworkDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateFrameworkDashboardApplication.class, args);
    }
}
