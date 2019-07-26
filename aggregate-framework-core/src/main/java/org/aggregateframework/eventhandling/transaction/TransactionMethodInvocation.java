package org.aggregateframework.eventhandling.transaction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.aggregateframework.eventhandling.EventHandlerHook;
import org.apache.commons.lang3.StringUtils;
import org.mengyun.commons.bean.FactoryBuilder;
import org.mengyun.compensable.transaction.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;

/**
 * Created by changming.xie on 10/26/17.
 */
public class TransactionMethodInvocation extends MethodInvocation implements KryoSerializable {
    static final Logger logger = LoggerFactory.getLogger(TransactionMethodInvocation.class);

    private static final long serialVersionUID = -5196077135452842976L;

    private String transactionCheckMethod;

    public TransactionMethodInvocation(Class<? extends Object> targetClass, String methodName, String transactionCheckMethod, Class<?>[] parameterTypes, Object[] params) {

        super(targetClass, methodName, parameterTypes, params);
        this.transactionCheckMethod = transactionCheckMethod;
    }

    @Override
    public Object proceed() throws Throwable {
        if (StringUtils.isNotEmpty(this.transactionCheckMethod)) {
            
            Object target = FactoryBuilder.factoryOf(this.getTargetClass()).getInstance();

            Method method = null;

            try {
                method = target.getClass().getMethod(this.transactionCheckMethod, this.getParameterTypes());
            } catch (NoSuchMethodException e) {
                StringBuilder stringBuilder = buildErrorMessage(target);
                logger.error(stringBuilder.toString(), e);
                throw e;
            }


            Object result = method.invoke(target, this.getArgs());

            if ((Boolean) result) {

                Object eventHandlerResult = null;
                try {
                    EventHandlerHook.INSTANCE.beforeEventHandler(this);
                    eventHandlerResult = super.proceed();
                } catch (Exception e) {
                    EventHandlerHook.INSTANCE.afterEventHandler(this, e);
                    throw e;
                }

                EventHandlerHook.INSTANCE.afterEventHandler(this, null);

                return eventHandlerResult;
            }
        }
        return null;
    }

    private StringBuilder buildErrorMessage(Object target) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nAgg-Framwork异常,没有找到下面这个方法\n");
        stringBuilder.append(target.getClass().getSimpleName() + "." + this.transactionCheckMethod);
        stringBuilder.append('(');
        for (Class klass : this.getParameterTypes()) {
            stringBuilder.append(klass.getSimpleName());
            stringBuilder.append(", ");
        }
        int len = stringBuilder.length();
        stringBuilder.delete(len-2,len);
        stringBuilder.append(")\n");
        stringBuilder.append("可能情况：\n");
        stringBuilder.append("1.该函数没有定义\n");
        stringBuilder.append("2.该函数写成了private,请改成public\n");
        stringBuilder.append("3.该函数参数类型和EventHandler中的参数类型不匹配\n");

        for (Method defMethod: target.getClass().getMethods()) {
            if (defMethod.getName().equals(this.transactionCheckMethod)) {
                stringBuilder.append("在类中找到了名字匹配的函数,其签名如下,请比较参数类型:\n");
                stringBuilder.append(defMethod.toString());
                break;
            }
        }
        stringBuilder.append('\n');
        return stringBuilder;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        String className = getTargetClass().getCanonicalName();
        int index = className.indexOf("$$EnhancerBySpringCGLIB");
        if (index >= 0) {
            className = className.substring(0, index);
        }
        kryo.writeObject(output, className);
        kryo.writeObject(output, getMethodName());
        kryo.writeObject(output, getParameterTypes());
        kryo.writeObject(output, getArgs());
        kryo.writeObject(output, transactionCheckMethod);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        String className = kryo.readObject(input, String.class);
        try {
            setTargetClass(this.getClass().getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        setMethodName(kryo.readObject(input, String.class));
        setParameterTypes(kryo.readObject(input, Class[].class));
        setArgs(kryo.readObject(input, Object[].class));
        transactionCheckMethod = kryo.readObject(input, String.class);
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        //out.defaultWriteObject();
        //System.out.println("write object");
        String className = getTargetClass().getCanonicalName();
        int index = className.indexOf("$$EnhancerBySpringCGLIB");
        if (index >= 0) {
            className = className.substring(0, index);
        }
        //System.out.println("write className: " + className);
        out.writeObject(className);
        //System.out.println("write MethodName: " + getMethodName());
        out.writeObject(getMethodName());
        //System.out.println("write ParameterTypes: ");
        out.writeObject(getParameterTypes());
        //System.out.println("write args: ");
        out.writeObject(getArgs());
        //System.out.println("write transactionCheckMethod: " + transactionCheckMethod);
        out.writeObject(transactionCheckMethod);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        //in.defaultReadObject();
        System.out.println("read object");
        String className = (String)in.readObject();
        System.out.println("read className: " + className);
        try {
            setTargetClass(this.getClass().getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        setMethodName((String)in.readObject());
        System.out.println("read MethodName: " + getMethodName());
        setParameterTypes((Class[])in.readObject());
        System.out.println("read ParameterTypes: ");
        setArgs((Object[])in.readObject());
        System.out.println("read args: ");
        transactionCheckMethod = (String)in.readObject();
        System.out.println("read transactionCheckMethod: " + transactionCheckMethod);
    }

    public String getTransactionCheckMethod() {
        return transactionCheckMethod;
    }

    public void setTransactionCheckMethod(String transactionCheckMethod) {
        this.transactionCheckMethod = transactionCheckMethod;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File file = new File("person.out");

        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(file));
        TransactionMethodInvocation person = new TransactionMethodInvocation(TransactionMethodInvocation.class,
                "main", "main", new Class[0], new Object[0]);
        oout.writeObject(person);
        oout.close();

        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(file));
        Object newPerson = oin.readObject(); // 没有强制转换到Person类型
        oin.close();
        System.out.println(newPerson);
    }
}
