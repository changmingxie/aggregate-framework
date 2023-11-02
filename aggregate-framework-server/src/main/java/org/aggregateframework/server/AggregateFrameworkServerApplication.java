package org.aggregateframework.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;

@SpringBootApplication(exclude = {QuartzAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class})
public class AggregateFrameworkServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateFrameworkServerApplication.class, args);
    }
}
