package org.aggregateframework.transaction.serializer.kryo;

import com.google.common.collect.Lists;
import org.aggregateframework.remoting.protocol.RemotingCommand;
import org.aggregateframework.remoting.protocol.RemotingCommandCode;
import org.aggregateframework.transaction.serializer.RemotingCommandSerializer;
import org.aggregateframework.utils.CollectionUtils;

import java.util.List;

public class RegisterableKryoRemotingCommandSerializer extends RegisterableKryoSerializer<RemotingCommand> implements RemotingCommandSerializer {


    static List<Class> remotingCommandClasses = Lists.newArrayList(
            RemotingCommand.class,
            RemotingCommandCode.class);

    public RegisterableKryoRemotingCommandSerializer() {
        this(remotingCommandClasses);
    }

    public RegisterableKryoRemotingCommandSerializer(int initPoolSize) {
        this(initPoolSize, remotingCommandClasses);
    }

    public RegisterableKryoRemotingCommandSerializer(List<Class> registerClasses) {
        super(CollectionUtils.merge(remotingCommandClasses, registerClasses));
    }

    public RegisterableKryoRemotingCommandSerializer(int initPoolSize, List<Class> registerClasses) {
        super(initPoolSize, CollectionUtils.merge(remotingCommandClasses, registerClasses));
    }

    public RegisterableKryoRemotingCommandSerializer(int initPoolSize, List<Class> registerClasses, boolean warnUnregisteredClasses) {
        super(initPoolSize, CollectionUtils.merge(remotingCommandClasses, registerClasses), warnUnregisteredClasses);
    }
}
