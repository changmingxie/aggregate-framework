package org.aggregateframework.basic.usage;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Nervose.Wu
 * @date 2022/6/16 16:19
 */
@Slf4j
@SpringBootApplication
@MapperScan("org.aggregateframework.basic.usage.dao")
@EnableAspectJAutoProxy(exposeProxy = true)
public class BasicUsageApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(BasicUsageApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("start");
    }
}
