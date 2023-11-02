package org.aggregateframework.sample.serializer;

import com.alibaba.fastjson.JSON;
import org.aggregateframework.sample.AbstractTestCase;
import org.aggregateframework.sample.quickstart.command.domain.event.OrderConfirmedEvent;
import org.aggregateframework.sample.quickstart.command.domain.factory.OrderFactory;
import org.aggregateframework.sample.quickstart.command.eventhandler.OrderHandler;
import org.aggregateframework.serializer.ObjectSerializer;
import org.aggregateframework.serializer.RegisterableKryoSerializer;
import org.aggregateframework.transaction.Invocation;
import org.aggregateframework.transaction.Participant;
import org.aggregateframework.transaction.Transaction;
import org.aggregateframework.xid.TransactionXid;
import org.junit.Test;

/**
 * Created by changming.xie on 9/18/17.
 */
public class SerializerTest extends AbstractTestCase {

//    private ObjectSerializer objectSerializer = new RegisterableKryoTransactionSerializer(Lists.newArrayList(
//            org.aggregateframework.sample.quickstart.command.domain.event.OrderConfirmedEvent.class
//    ));

//    private ObjectSerializer objectSerializer = new RegisterableKryoTransactionSerializer();


    private ObjectSerializer objectSerializer = new RegisterableKryoSerializer();

    @Test
    public void given_transaction_when_serialize_and_compare_then_all_cost_time_printed() {

        Transaction transaction = new Transaction(TransactionXid.withUniqueIdentity(null));

        transaction.enlistParticipant(
                new Participant(
                        new Invocation(
                                OrderHandler.class, "handleOrderCreatedEvent", "checkOrderIsConfirmed",
                                new Class[]{OrderConfirmedEvent.class}, new Object[]{new OrderConfirmedEvent(OrderFactory.buildOrder(1, 1001, 1))}
                        )));

        String jsons = JSON.toJSONString(transaction);

        byte[] jsonBytes = jsons.getBytes();
        System.out.println("json size:" + jsonBytes.length);

        byte[] bytes = objectSerializer.serialize(transaction);

        System.out.println("kryo size:" + bytes.length);

        Transaction deserialized = (Transaction) objectSerializer.deserialize(bytes);


        long totalTime = 0;

        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {

            objectSerializer.serialize(transaction);
        }

        totalTime = (System.currentTimeMillis() - currentTime);

        System.out.println("1000 count serialize cost time:" + totalTime);
    }
}
