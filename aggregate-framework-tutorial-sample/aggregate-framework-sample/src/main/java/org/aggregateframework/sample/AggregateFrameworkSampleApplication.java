package org.aggregateframework.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations = {"classpath:config/spring/local/*.xml"})
public class AggregateFrameworkSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateFrameworkSampleApplication.class, args);
    }
}
