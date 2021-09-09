package org.aggregateframework.test;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.aggregateframework.eventhandling.processor.async.EventProcessThreadFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class DisrupterTest {

    static class MyEvent {

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        int seq;


    }

    public void testHandler(List<MyEvent> events) {

    }

    public static void main(String... args) throws IOException {

        Method[] methods = DisrupterTest.class.getMethods();

        Method foundMethod = null;

        for (Method method : methods) {
            if (method.getName().equals("testHandler")) {
                foundMethod = method;
            }
        }

        Class[] classes = foundMethod.getParameterTypes();

        final ThreadFactory threadFactory = new EventProcessThreadFactory("AsyncDisruptorEventProcessThreadFactory", true, Thread.NORM_PRIORITY) {
            @Override
            public Thread newThread(final Runnable r) {
                final Thread result = super.newThread(r);
                return result;
            }
        };

        WaitStrategy waitStrategy = new BlockingWaitStrategy();

//        Disruptor<MyEvent> disruptor0 = new Disruptor<MyEvent>();

        Disruptor<MyEvent> disruptor = new Disruptor<MyEvent>(new EventFactory<MyEvent>() {
            @Override
            public MyEvent newInstance() {
                return new MyEvent();
            }
        }, 8, threadFactory, ProducerType.MULTI, waitStrategy);

        ExceptionHandler<MyEvent> errorHandler = new ExceptionHandler<MyEvent>() {
            @Override
            public void handleEventException(Throwable throwable, long l, MyEvent myEvent) {

            }

            @Override
            public void handleOnStartException(Throwable throwable) {

            }

            @Override
            public void handleOnShutdownException(Throwable throwable) {

            }
        };

        disruptor.setDefaultExceptionHandler(errorHandler);

        EventHandler eventHandler = new EventHandler<MyEvent>() {
            @Override
            public void onEvent(final MyEvent event, final long sequence,
                                final boolean endOfBatch) throws Exception {

                Thread.sleep(300);
                System.out.println("myevent seq:" + event.getSeq() + ", disrupter seq:" + sequence + ", endOfBatch:" + endOfBatch + ", thread:" + Thread.currentThread().getName());

            }
        };

        final EventHandler[] asyncEventHandlers = {eventHandler, eventHandler,
                eventHandler, eventHandler,
                eventHandler, eventHandler,
                eventHandler, eventHandler};
//        disruptor.handleEventsWith(asyncEventHandlers);

        WorkHandler<MyEvent> workHandler = new WorkHandler<MyEvent>() {
            @Override
            public void onEvent(MyEvent myEvent) throws Exception {
                Thread.sleep(300);
                System.out.println("myevent seq:" + myEvent.getSeq() + ", thread:" + Thread.currentThread().getName());
            }
        };


        WorkHandler[] workHandlers = {workHandler, workHandler, workHandler, workHandler};

        disruptor.handleEventsWithWorkerPool(workHandlers);

        disruptor.start();

        for (int i = 0; i < 100; i++) {

            int finalI = i;

//             boolean result = disruptor.getRingBuffer().tryPublishEvent(new EventTranslator<MyEvent>() {
//                @Override
//                public void translateTo(MyEvent asyncEvent, long l) {
//                    asyncEvent.setSeq(finalI);
//                }
//            });
//
//            if(!result) {
//                System.out.println("publish failed. i:" +i );
//            }

            disruptor.getRingBuffer().publishEvent(new EventTranslator<MyEvent>() {
                @Override
                public void translateTo(MyEvent asyncEvent, long l) {
                    asyncEvent.setSeq(finalI);
                }
            });
        }

        System.out.println("publish done.");

        System.in.read();
    }

    public static boolean getParamClass(Method method) {

        Class[] classes = method.getParameterTypes();

        Type type = classes[0];


        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            if ((parameterizedType.getRawType() instanceof Class)
                    && Collection.class.isAssignableFrom((Class) parameterizedType.getRawType())) {
                for (Type actualType : parameterizedType.getActualTypeArguments()) {
                   return true;
                }
            }
        }

        return false;
    }
}
