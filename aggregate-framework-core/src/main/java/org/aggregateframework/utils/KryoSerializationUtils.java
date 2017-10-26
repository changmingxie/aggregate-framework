package org.aggregateframework.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializationUtils {

    public static int PRE_INIT_KRYO_INSTANCE = 300;

    static KryoFactory factory = new KryoFactory() {
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(false);
            //Fix the NPE bug when deserializing Collections.
            ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                    .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

            return kryo;
        }
    };

    static KryoPool pool = new KryoPool.Builder(factory).softReferences().build();

    static {

        for (int i = 0; i < PRE_INIT_KRYO_INSTANCE; i++) {
            Kryo kryo = pool.borrow();
            pool.release(kryo);
        }

    }

    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(false);
            //Fix the NPE bug when deserializing Collections.
            ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                    .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

            return kryo;
        }
    };

    public static Kryo getInstance() {
        return kryoLocal.get();
    }

    public static <T> byte[] writeToByteArray(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, obj);
        output.flush();

        return byteArrayOutputStream.toByteArray();
    }

    public static <T> T readFromByteArray(byte[] byteArray) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        Input input = new Input(byteArrayInputStream);

        Kryo kryo = getInstance();
        return (T) kryo.readClassAndObject(input);
    }

    public static <T> T clone(final T object) {

//        return getInstance().copy(object);

        return pool.run(new KryoCallback<T>() {
            public T execute(Kryo kryo) {
                return kryo.copy(object);
            }
        });
    }
}