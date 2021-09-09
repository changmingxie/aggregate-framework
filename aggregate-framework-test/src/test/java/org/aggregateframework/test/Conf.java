package org.aggregateframework.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Lee on 2020/5/13 15:06.
 */
@Configuration
@ImportResource(value = "classpath:/config/spring/common/aggregate-framework-context.xml")
public class Conf {
    
    
    @Bean
    public EventHand x() {
        return new EventHand();
    }
}
