package org.aggregateframework.test;

import com.google.common.collect.Lists;
import org.aggregateframework.domainevent.EventMessage;
import org.aggregateframework.domainevent.SimpleEventMessage;
import org.aggregateframework.eventbus.EventBus;
import org.aggregateframework.eventbus.SimpleEventBus;
import org.aggregateframework.transaction.LocalTransactionExecutor;
import org.aggregateframework.transaction.LocalTransactionState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Lee on 2020/5/13 14:51.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class AnnotationEventListenerBeanPostProcessorTest {
    
    private ConfigurableApplicationContext ac;
    
    EventBus bus = SimpleEventBus.INSTANCE;
    
    @Before
    public void setUp() {
        
        ac = new AnnotationConfigApplicationContext(Conf.class);
        
    }
    
    @Test
    public void se() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        bus.publishInTransaction(Lists.newArrayList(new SimpleEventMessage<>(new EventHand.TestEvent())), new LocalTransactionExecutor() {
            @Override
            public LocalTransactionState executeLocalTransactionBranch(List<EventMessage> events) {
                latch.countDown();
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        latch.await();
        
    }
    
    @After
    public void clos() {
        ac.close();
    }
    
}